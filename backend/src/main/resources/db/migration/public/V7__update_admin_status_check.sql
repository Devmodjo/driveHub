-- V7 — Ajout de EMAIL_PENDING dans la contrainte admin_status
ALTER TABLE public.platform_admin
DROP CONSTRAINT IF EXISTS platform_admin_status_check;

ALTER TABLE public.platform_admin
ADD CONSTRAINT platform_admin_status_check CHECK (
    admin_status = ANY (ARRAY[
        'EMAIL_PENDING',
        'ACTIVE',
        'PENDING',
        'SUSPENDED',
        'DISABLED'
    ])
);