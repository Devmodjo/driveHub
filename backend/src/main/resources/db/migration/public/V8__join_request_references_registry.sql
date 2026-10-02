-- ============================================================
-- V8 — Les demandes d'adhésion référencent le REGISTRE des auto-écoles
-- ------------------------------------------------------------
-- school_join_request.driving_school_id contient l'id d'un DrivingSchoolRegistry
-- (c'est ce que le code enregistre), mais la contrainte pointait vers
-- public.driving_school : toute demande d'adhésion échouait (violation de FK).
-- La table driving_school vit désormais dans le schéma de chaque auto-école.
-- NOT VALID : n'échoue pas sur d'éventuelles lignes anciennes incohérentes.
-- ============================================================
ALTER TABLE public.school_join_request
    DROP CONSTRAINT IF EXISTS school_join_request_school_fk;

ALTER TABLE public.school_join_request
    ADD CONSTRAINT school_join_request_registry_fk
        FOREIGN KEY (driving_school_id) REFERENCES public.driving_school_registry (id) NOT VALID;

CREATE INDEX IF NOT EXISTS idx_join_request_school_status
    ON public.school_join_request (driving_school_id, join_status);
