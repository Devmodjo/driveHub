#!/usr/bin/env bash
# =====================================================================
#  Test de bout en bout du backend DriveHub (multi-tenant par schéma).
#
#  Prérequis : API démarrée (mvn spring-boot:run), curl, jq, psql.
#  Usage     : ./scripts/e2e-multitenant.sh [URL_API] [URL_JDBC_PSQL]
#     ex.    : ./scripts/e2e-multitenant.sh http://localhost:8082 "postgresql://postgres:root@localhost:5432/drivehubDB"
#
#  Les variables MOCK_ROOT_USERNAME / MOCK_ROOT_PASSWORD doivent être celles
#  utilisées au démarrage de l'API (compte ROOT créé par RootAdminerSeeder).
#
#  Scénario :
#   1. deux moniteurs s'inscrivent, vérifient leur email et demandent la création d'une auto-école ;
#   2. le ROOT approuve : un schéma PostgreSQL est créé pour chaque auto-école ;
#   3. un élève s'inscrit et rejoint l'auto-école A ; le moniteur A approuve ;
#   4. gestion métier dans le tenant A : véhicule, cours, examen, réservation, paiement Mobile Money ;
#   5. isolation : le moniteur B ne peut pas lire les données de A, même en changeant X-Tenant-ID.
# =====================================================================
set -euo pipefail

API="${1:-http://localhost:8082}"
DB="${2:-postgresql://postgres:root@localhost:5432/drivehubDB}"
RUN=$(date +%s%N | tail -c 7)
PASS=0
ok()   { PASS=$((PASS + 1)); echo "  [OK] $1"; }
fail() { echo "  [ECHEC] $1"; exit 1; }

# call METHODE CHEMIN [JETON] [TENANT] [CORPS] -> "CODE CORPS"
call() {
  local args=(-s -o /tmp/e2e_body -w "%{http_code}" -X "$1" "$API$2" -H "Content-Type: application/json")
  [[ -n "${3:-}" ]] && args+=(-H "Authorization: Bearer $3")
  [[ -n "${4:-}" ]] && args+=(-H "X-Tenant-ID: $4")
  [[ -n "${5:-}" ]] && args+=(-d "$5")
  echo "$(curl "${args[@]}") $(cat /tmp/e2e_body)"
}
expect() { [[ "${2%% *}" == "$1" ]] && ok "$3" || fail "$3 (attendu $1, reçu : ${2:0:300})"; }
body() { echo "${1#* }"; }

verify_email() { # email -> valide le compte via le jeton stocké en base
  local token
  token=$(psql "$DB" -tAc "SELECT t.token FROM public.email_verification_tokens t JOIN public._users u ON u.id = t.user_id WHERE u.email = '$1'")
  expect 200 "$(call GET "/api/auth/verify-email?token=$token")" "Vérification email $1"
}
login() { body "$(call POST /api/auth/login "" "" "{\"email\":\"$1\",\"password\":\"Password123\"}")" | jq -r .token; }
register() { # role email
  local profile="\"firstname\":\"Test\",\"lastname\":\"$1\",\"email\":\"$2\",\"password\":\"Password123\",\"phoneNumber\":\"+237699000000\",\"gender\":\"MALE\",\"nationality\":\"Camerounaise\",\"residenceCity\":\"Douala\",\"dateOfBirth\":\"1995-04-12\",\"acceptPrivacyPolicy\":true"
  expect 201 "$(call POST "/api/auth/register/$1" "" "" "{$profile}")" "Inscription $1 $2"
}
create_school() { # jeton nom — présentation de 1500 caractères (non-régression : limite de 255 en base)
  local long_desc; long_desc=$(printf 'a%.0s' $(seq 1 1500))
  expect 201 "$(call POST /api/driving-schools/request "$1" "" "{\"name\":\"$2\",\"email\":\"contact-$RUN@ecole.cm\",\"country\":\"Cameroun\",\"city\":\"Douala\",\"phoneNumber\":\"+237699111222\",\"address\":\"Akwa\",\"description\":\"$long_desc\",\"websiteUrl\":null,\"whatsappNumber\":null}")" "Demande de création (présentation de 1500 caractères) : $2"
}

echo "1. Moniteurs et demandes d'auto-école"
MA="monitor-a-$RUN@test.cm"; MB="monitor-b-$RUN@test.cm"; ST="student-$RUN@test.cm"
register monitor "$MA"; register monitor "$MB"
verify_email "$MA"; verify_email "$MB"
TA=$(login "$MA"); TB=$(login "$MB")
create_school "$TA" "Auto-École Le Volant $RUN"
expect 409 "$(call POST /api/driving-schools/request "$TB" "" "{\"name\":\"Auto-École Le Volant $RUN\",\"email\":\"x-$RUN@ecole.cm\",\"phoneNumber\":\"+237699000001\",\"address\":\"Bonapriso\"}")" "Nom d'auto-école déjà pris : refusé"
TB_RESP=$(call POST /api/driving-schools/request "$TB" "" "{\"name\":\"École Bis $RUN\",\"email\":\"b-$RUN@ecole.cm\",\"country\":\"Cameroun\",\"city\":\"Yaoundé\",\"phoneNumber\":\"+237699333444\",\"address\":\"Bastos\",\"description\":\"B\"}")
expect 201 "$TB_RESP" "Demande de création : École Bis"
echo "2. Approbation par le ROOT (création des schémas)"
ROOT=$(body "$(call POST /api/platform/admin/login "" "" "{\"email\":\"$MOCK_ROOT_USERNAME\",\"password\":\"$MOCK_ROOT_PASSWORD\"}")" | jq -r .token)
PENDING=$(body "$(call GET /api/platform/registries/pending "$ROOT")")
RA=$(echo "$PENDING" | jq -r ".[] | select(.schoolName==\"Auto-École Le Volant $RUN\") | .id")
RB=$(echo "$PENDING" | jq -r ".[] | select(.schoolName==\"École Bis $RUN\") | .id")
[[ -n "$RA" && -n "$RB" ]] && ok "Demandes visibles par le ROOT" || fail "Demandes introuvables : $PENDING"
expect 202 "$(call PATCH "/api/platform/registries/$RA/approve" "$ROOT")" "Approbation auto-école A"
expect 202 "$(call PATCH "/api/platform/registries/$RB/approve" "$ROOT")" "Approbation auto-école B"
SA=$(psql "$DB" -tAc "SELECT schema_name FROM public.driving_school_registry WHERE id = '$RA'")
SB=$(psql "$DB" -tAc "SELECT schema_name FROM public.driving_school_registry WHERE id = '$RB'")
[[ $(psql "$DB" -tAc "SELECT count(*) FROM $SA.driving_school") == 1 ]] && ok "Schéma $SA créé avec sa fiche auto-école" || fail "Schéma $SA incomplet"
[[ $(psql "$DB" -tAc "SELECT count(*) FROM $SA.monitors") == 1 ]] && ok "Moniteur fondateur créé dans $SA" || fail "Moniteur fondateur absent"
[[ $(psql "$DB" -tAc "SELECT active FROM public.tenants WHERE code = '$SA'") == t ]] && ok "Tenant $SA enregistré et actif" || fail "Tenant non enregistré"
TA=$(login "$MA"); TB=$(login "$MB")   # nouveaux jetons : ils portent maintenant le tenant
[[ $(body "$(call GET /api/driving-schools/public/all)" | jq "[.[] | select(.id==\"$RA\")] | length") == 1 ]] && ok "Auto-école A visible dans le catalogue public" || fail "Catalogue public incorrect"

echo "3. Élève : inscription, adhésion, validation par le moniteur"
register student "$ST"; verify_email "$ST"
TS=$(login "$ST")
expect 200 "$(call POST /api/join-school/public "$TS" "" "{\"drivingSchoolId\":\"$RA\",\"role\":\"STUDENT\"}")" "Demande d'adhésion à A"
REQ=$(body "$(call GET /api/join-school/admin/pending "$TA")" | jq -r '.content[0].requestId')
expect 403 "$(call POST "/api/join-school/admin/$REQ/approve" "$TB")" "Le moniteur B ne peut pas valider une demande de A"
expect 200 "$(call POST "/api/join-school/admin/$REQ/approve" "$TA")" "Le moniteur A valide l'adhésion"
TS=$(login "$ST")
R=$(call GET /api/students/me "$TS" "$SA"); expect 200 "$R" "Dossier élève dans le tenant A"
STUDENT_ID=$(body "$R" | jq -r .id)

echo "4. Gestion métier dans le tenant A"
R=$(call POST /api/vehicles "$TA" "$SA" '{"matriculation":"lt 452 ab","model":"Toyota Yaris","state":"DISPOSABLE"}'); expect 201 "$R" "Ajout d'un véhicule"
VEHICLE_ID=$(body "$R" | jq -r .id)
expect 409 "$(call POST /api/vehicles "$TA" "$SA" '{"matriculation":"LT 452 AB","model":"Autre","state":"DISPOSABLE"}')" "Immatriculation en double refusée"
expect 201 "$(call POST /api/courses "$TA" "$SA" '{"title":"Priorités","content":"Priorité à droite"}')" "Publication d'un cours"
expect 200 "$(call GET /api/courses "$TS" "$SA")" "L'élève lit les cours"
expect 200 "$(call PUT "/api/students/$STUDENT_ID" "$TA" "$SA" '{"licenseCategory":"B"}')" "Le moniteur complète le dossier (permis B)"
R=$(call POST /api/exams "$TA" "$SA" '{"dateExams":"2030-06-15T08:00:00","category":"B"}'); expect 201 "$R" "Création d'une session d'examen"
EXAM_ID=$(body "$R" | jq -r .id)
expect 201 "$(call POST "/api/exams/$EXAM_ID/inscriptions" "$TA" "$SA" "{\"studentId\":\"$STUDENT_ID\"}")" "Inscription de l'élève à l'examen"
expect 409 "$(call POST "/api/exams/$EXAM_ID/inscriptions" "$TA" "$SA" "{\"studentId\":\"$STUDENT_ID\"}")" "Double inscription refusée"
MONITOR_ID=$(body "$(call GET /api/monitors "$TS" "$SA")" | jq -r '.[0].id')
SLOT='{"monitorId":"'$MONITOR_ID'","vehicleId":"'$VEHICLE_ID'","dateTime":"2030-01-10T09:00:00","types":"CONDUITE"}'
R=$(call POST /api/reservations "$TS" "$SA" "$SLOT"); expect 201 "$R" "L'élève réserve une leçon"
[[ $(body "$R" | jq -r .reservationStatus) == PENDING ]] && ok "Réservation de l'élève en attente" || fail "Statut inattendu"
expect 409 "$(call POST /api/reservations "$TA" "$SA" "{\"studentId\":\"$STUDENT_ID\",${SLOT:1}")" "Créneau déjà pris refusé"
expect 200 "$(call PATCH "/api/reservations/$(body "$R" | jq -r .id)/confirm" "$TA" "$SA")" "Le moniteur confirme la réservation"
# Paiement Mobile Money (passerelle SIMULATED : même déroulé que Campay, sans argent réel)
R=$(call POST /api/payments "$TS" "$SA" '{"amount":50000,"method":"MOMO","motif":"INSCRIPTION","phoneNumber":"+237 677 11 22 33"}')
expect 201 "$R" "L'élève paie 50 000 FCFA par Mobile Money"
PAY_ID=$(body "$R" | jq -r .id)
[[ $(body "$R" | jq -r .paymentStatus) == PENDING ]] && ok "Paiement en attente de validation sur le téléphone" || fail "Statut initial inattendu"
[[ $(body "$(call POST "/api/payments/$PAY_ID/refresh" "$TS" "$SA")" | jq -r .paymentStatus) == VALIDATE ]] && ok "Vérification : paiement confirmé" || fail "Paiement non confirmé"
R=$(call POST /api/payments "$TS" "$SA" '{"amount":10000,"method":"OM","motif":"EXAMS","phoneNumber":"+237 699 00 0000"}')
[[ $(body "$(call POST "/api/payments/$(body "$R" | jq -r .id)/refresh" "$TS" "$SA")" | jq -r .paymentStatus) == REJECTED ]] && ok "Numéro finissant par 0000 : paiement refusé (simulation)" || fail "Refus simulé non appliqué"
expect 400 "$(call POST /api/payments "$TS" "$SA" '{"amount":1000,"method":"MOMO","motif":"EXAMS"}')" "Numéro Mobile Money obligatoire"
expect 400 "$(call POST /api/payments "$TS" "$SA" '{"amount":1000,"method":"CASH","motif":"EXAMS"}')" "Paiement espèces refusé côté élève"
expect 201 "$(call POST /api/payments "$TA" "$SA" "{\"studentsId\":\"$STUDENT_ID\",\"amount\":25000,\"method\":\"CASH\",\"motif\":\"INSCRIPTION\"}")" "Le moniteur enregistre 25 000 FCFA reçus en espèces"
expect 200 "$(call GET "/api/webhooks/campay?status=SUCCESSFUL&reference=x&external_reference=$SA:$PAY_ID&signature=faux")" "Webhook non signé : accepté sans effet"
[[ $(body "$(call GET /api/payments/summary "$TA" "$SA")" | jq -r .totalValidated) == 75000* ]] && ok "Total encaissé : 75 000 FCFA" || fail "Total incorrect"

echo "5. Sécurité et isolation des tenants"
expect 401 "$(call GET /api/students "$TB" "$SA")" "Moniteur B avec X-Tenant-ID de A : refusé"
R=$(call GET /api/students "$TB" "$SB"); expect 200 "$R" "Moniteur B dans son propre tenant"
[[ $(body "$R" | jq -r .totalElements) == 0 ]] && ok "B ne voit aucun élève de A" || fail "Fuite de données entre tenants"
expect 403 "$(call GET /api/students "$TS" "$SA")" "Un élève n'accède pas à la liste des élèves"
expect 400 "$(call GET /api/students "$TA" "x;DROP SCHEMA public")" "Nom de tenant invalide rejeté"
expect 401 "$(call GET /api/vehicles "" "$SA")" "Accès refusé sans jeton (401)"

echo "6. Déconnexion"
expect 200 "$(call GET /api/auth/me "$TB")" "Jeton du moniteur B valide avant la déconnexion"
expect 200 "$(call POST /api/auth/logout "$TB")" "Déconnexion du moniteur B"
expect 401 "$(call GET /api/auth/me "$TB")" "Jeton refusé après la déconnexion"
expect 401 "$(call GET /api/students "$TB" "$SB")" "Jeton refusé aussi sur les routes du tenant"
TB=$(login "$MB")
expect 200 "$(call GET /api/students "$TB" "$SB")" "Nouvelle connexion : nouveau jeton valide"


echo "7. Emails du back-office"
R=$(call POST /api/platform/emails "$ROOT" "" "{\"audience\":\"SCHOOL_MEMBERS\",\"schoolId\":\"$RA\",\"subject\":\"Test e2e $RUN\",\"message\":\"Bonjour, ceci est un message de test.\"}")
expect 202 "$R" "Email aux membres de l'auto-école A"
[[ $(body "$R" | jq -r .recipientCount) == 2 ]] && ok "Destinataires : le moniteur A et l'élève accepté" || fail "Nombre de destinataires inattendu : $(body "$R")"
expect 400 "$(call POST /api/auth/register/student "" "" '{"firstname":"X","email":"x-'$RUN'@test.cm","password":"Password123","phoneNumber":"+237699000000","gender":"MALE","nationality":"Camerounaise","residenceCity":"Douala","dateOfBirth":"1995-04-12"}')" "Inscription refusée sans consentement"

echo
echo "Tous les tests sont passés ($PASS vérifications)."
