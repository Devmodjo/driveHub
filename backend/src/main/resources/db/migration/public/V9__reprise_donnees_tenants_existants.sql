-- ============================================================
-- V9 — Reprise des auto-écoles approuvées AVANT les corrections
-- ------------------------------------------------------------
-- Avec l'ancien code :
--   * l'approbation créait bien le schéma, mais ne remplissait pas public.tenants
--     (toutes les requêtes métier étaient refusées : "Tenant invalide") ;
--   * la fiche auto-école était enregistrée dans public.driving_school au lieu du schéma.
-- Cette migration rattrape ces données : AUCUNE base n'a besoin d'être supprimée.
-- Elle est sans effet sur une base neuve, et peut être rejouée sans risque (idempotente).
-- ============================================================
DO $$
DECLARE
    reg RECORD;
BEGIN
    FOR reg IN
        SELECT r.schema_name, r.admin_id
        FROM public.driving_school_registry r
        WHERE r.driving_school_status IN ('APPROVED', 'ACTIVE')
          -- même règle de nommage que TenantSchemas (protection contre l'injection SQL)
          AND r.schema_name ~ '^[a-z][a-z0-9_]{1,62}$'
          AND EXISTS (SELECT 1 FROM information_schema.tables t
                      WHERE t.table_schema = r.schema_name AND t.table_name = 'driving_school')
    LOOP
        -- 1. Le tenant devient valide pour TenantResolutionFilter
        INSERT INTO public.tenants (code, active) VALUES (reg.schema_name, true)
        ON CONFLICT (code) DO NOTHING;

        -- 2. La fiche auto-école est recopiée dans son schéma (si le schéma n'en a pas encore)
        EXECUTE format(
            'INSERT INTO %1$I.driving_school (id, created_on, deleted, deleted_at, last_update_on, status,
                    address, city, country, created_at, description, driving_school_status, email, name,
                    phone_number, website_url, whatsapp_number, user_id)
             SELECT d.id, d.created_on, d.deleted, d.deleted_at, d.last_update_on, d.status,
                    d.address, d.city, d.country, d.created_at, d.description, d.driving_school_status, d.email, d.name,
                    d.phone_number, d.website_url, d.whatsapp_number, d.user_id
             FROM public.driving_school d
             WHERE d.user_id = $1
               AND NOT EXISTS (SELECT 1 FROM %1$I.driving_school)
             LIMIT 1', reg.schema_name)
        USING reg.admin_id;

        -- 3. Le moniteur fondateur est recopié dans son schéma
        EXECUTE format(
            'INSERT INTO %1$I.monitors (id, created_on, deleted, last_update_on, status, date_of_birth, gender,
                    nationality, phone_number, residence_city, driving_school_id, user_id)
             SELECT gen_random_uuid(), now(), false, now(), 0, m.date_of_birth, m.gender,
                    m.nationality, m.phone_number, m.residence_city,
                    (SELECT ds.id FROM %1$I.driving_school ds LIMIT 1), m.user_id
             FROM public.monitors m
             WHERE m.user_id = $1
               AND NOT EXISTS (SELECT 1 FROM %1$I.monitors x WHERE x.user_id = $1)
             LIMIT 1', reg.schema_name)
        USING reg.admin_id;
    END LOOP;
END $$;
