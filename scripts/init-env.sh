#!/usr/bin/env bash
# =====================================================================
#  Prépare backend/.env pour un premier lancement en local.
#
#  Usage (depuis la racine du projet) :  ./scripts/init-env.sh
#
#  - copie backend/.env.example vers backend/.env (sans jamais écraser un .env existant) ;
#  - génère les secrets : JWT_SECRET_KEY, DATA_ENCRYPTION_KEY et le mot de passe du compte ROOT ;
#  - il ne reste qu'à vérifier les lignes DB_* (votre PostgreSQL) et, si vous voulez de vrais emails, MAIL_*.
# =====================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/backend/.env"
EXAMPLE="$ROOT_DIR/backend/.env.example"

if [[ -f "$ENV_FILE" ]]; then
  echo "backend/.env existe déjà : rien n'est modifié (supprimez-le pour repartir de zéro)."
  exit 0
fi

# Octets aléatoires encodés en Base64 (openssl, sinon python3, sinon /dev/urandom)
random_base64() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 "$1"
  elif command -v python3 >/dev/null 2>&1; then
    python3 -c "import base64,os,sys;print(base64.b64encode(os.urandom(int(sys.argv[1]))).decode())" "$1"
  else
    head -c "$1" /dev/urandom | base64
  fi | tr -d '\n'
}

JWT=$(random_base64 48)
DATA_KEY=$(random_base64 32)
ROOT_PASSWORD="Root-$(random_base64 9 | tr -dc 'A-Za-z0-9' | head -c 10)"

# Remplacement ligne par ligne (les valeurs Base64 peuvent contenir / et +)
while IFS= read -r line || [[ -n "$line" ]]; do
  case "$line" in
    JWT_SECRET_KEY=*)      echo "JWT_SECRET_KEY=$JWT" ;;
    DATA_ENCRYPTION_KEY=*) echo "DATA_ENCRYPTION_KEY=$DATA_KEY" ;;
    MOCK_ROOT_PASSWORD=*)  echo "MOCK_ROOT_PASSWORD=$ROOT_PASSWORD" ;;
    *)                     echo "$line" ;;
  esac
done < "$EXAMPLE" > "$ENV_FILE"

cat <<MSG
backend/.env créé.

  Compte administrateur du back-office (/backoffice/login) :
    email        : $(grep '^MOCK_ROOT_USERNAME=' "$ENV_FILE" | cut -d= -f2)
    mot de passe : $ROOT_PASSWORD

  À vérifier dans backend/.env :
    DB_URL / DB_USERNAME / DB_PASSWORD  -> votre PostgreSQL (la base doit exister : CREATE DATABASE drivehub;)
    MAIL_*                              -> facultatif en local : sans serveur d'emails, les liens
                                           (vérification, invitation) s'affichent dans la console du backend.

  Gardez une copie de DATA_ENCRYPTION_KEY : elle chiffre les justificatifs.
MSG
