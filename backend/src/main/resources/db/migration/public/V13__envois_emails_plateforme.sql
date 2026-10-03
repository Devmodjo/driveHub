-- ============================================================
-- V13 — Historique des emails envoyés depuis le back-office
-- ------------------------------------------------------------
-- Le propriétaire de la plateforme peut écrire aux auto-écoles, aux moniteurs et aux élèves
-- (en groupe ou individuellement). Chaque envoi est tracé : qui, quand, à qui, combien de destinataires.
-- ============================================================
CREATE TABLE IF NOT EXISTS public.platform_email_campaigns (
    id               uuid PRIMARY KEY,
    subject          character varying(150) NOT NULL,
    message          text                   NOT NULL,
    audience         character varying(30)  NOT NULL,
    school_id        uuid,
    school_name      character varying(150),
    recipient_count  integer                NOT NULL,
    sent_by_email    character varying(150) NOT NULL,
    sent_at          timestamp(6)           NOT NULL,
    CONSTRAINT platform_email_campaigns_audience_check CHECK (
        audience IN ('ALL_SCHOOLS', 'ALL_MONITORS', 'ALL_STUDENTS', 'SCHOOL_MEMBERS', 'INDIVIDUAL')
    )
);

CREATE INDEX IF NOT EXISTS idx_platform_email_campaigns_sent_at ON public.platform_email_campaigns (sent_at DESC);
