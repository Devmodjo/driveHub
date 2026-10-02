-- ============================================================
-- V3 (tenant) — Corrections des champs métier
-- Appliquée automatiquement à TOUTES les auto-écoles au démarrage
-- (TenantMigrationRunner) et à chaque nouvelle auto-école approuvée.
-- ============================================================

-- Montants : jamais de double precision pour de l'argent (erreurs d'arrondi).
ALTER TABLE payments ALTER COLUMN amount TYPE numeric(12, 2) USING amount::numeric(12, 2);

-- Réservation : la date est CHOISIE (créneau), elle n'est plus la date de création.
UPDATE reservations SET date_time = COALESCE(created_on, now()) WHERE date_time IS NULL;
ALTER TABLE reservations ALTER COLUMN date_time SET NOT NULL;

-- Une leçon de conduite se fait avec un véhicule.
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS vehicle_id uuid;
ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_vehicle_fk;
ALTER TABLE reservations ADD CONSTRAINT reservations_vehicle_fk
    FOREIGN KEY (vehicle_id) REFERENCES vehicles (id);

-- Un compte utilisateur = au plus une fiche élève / moniteur par auto-école.
CREATE UNIQUE INDEX IF NOT EXISTS uk_students_user ON students (user_id) WHERE deleted = false;
CREATE UNIQUE INDEX IF NOT EXISTS uk_monitors_user ON monitors (user_id) WHERE deleted = false;

-- Pas deux véhicules avec la même immatriculation, pas deux inscriptions au même examen.
CREATE UNIQUE INDEX IF NOT EXISTS uk_vehicles_matriculation ON vehicles (matriculation) WHERE deleted = false;
CREATE UNIQUE INDEX IF NOT EXISTS uk_exams_inscription ON exams_inscription (exams_id, student_id) WHERE deleted = false;

-- Index des recherches fréquentes.
CREATE INDEX IF NOT EXISTS idx_reservations_monitor_date ON reservations (monitor_id, date_time);
CREATE INDEX IF NOT EXISTS idx_reservations_student ON reservations (student_id);
CREATE INDEX IF NOT EXISTS idx_payments_student ON payments (student_id);
