-- ============================================================
-- V15 — Abonnements des auto-écoles (environnement préparé, facturation DÉSACTIVÉE)
-- ------------------------------------------------------------
-- Fonctionnement prévu (détail : docs/ABONNEMENTS-ET-PAIEMENTS.md) :
--   1. à la validation d'une auto-école, un abonnement « TRIAL » démarre pour 15 jours ;
--   2. plus tard (billing.enabled = true), une facture sera émise à la fin de l'essai, puis chaque mois,
--      et payée par Mobile Money (Campay) ;
--   3. les factures (subscription_invoices) serviront à la comptabilité du back-office.
-- Aujourd'hui : seule la période d'essai est enregistrée ; aucune facture n'est créée, rien n'est bloqué.
-- ============================================================

-- Offres affichées sur la vitrine (prix en francs CFA, sans centimes)
CREATE TABLE IF NOT EXISTS public.subscription_plans (
    code               character varying(30)  PRIMARY KEY,
    name               character varying(100) NOT NULL,
    monthly_price_fcfa numeric(12, 0),                     -- NULL : sur devis
    active             boolean                NOT NULL DEFAULT true
);

INSERT INTO public.subscription_plans (code, name, monthly_price_fcfa) VALUES
    ('ESSENTIEL',  'Essentiel',  25000),
    ('BUSINESS',   'Business',   50000),
    ('SUR_MESURE', 'Sur mesure', NULL)
ON CONFLICT (code) DO NOTHING;

-- Un abonnement par auto-école
CREATE TABLE IF NOT EXISTS public.school_subscriptions (
    id                   uuid PRIMARY KEY,
    registry_id          uuid                  NOT NULL UNIQUE REFERENCES public.driving_school_registry (id),
    plan_code            character varying(30) NOT NULL REFERENCES public.subscription_plans (code),
    status               character varying(20) NOT NULL,
    trial_started_at     timestamp(6)          NOT NULL,
    trial_ends_at        timestamp(6)          NOT NULL,
    current_period_start timestamp(6),
    current_period_end   timestamp(6),
    created_at           timestamp(6)          NOT NULL,
    updated_at           timestamp(6)          NOT NULL,
    CONSTRAINT school_subscriptions_status_check CHECK (
        status IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'SUSPENDED', 'CANCELLED')
    )
);

-- Factures (vide tant que billing.enabled = false) : base de la future comptabilité du back-office.
-- Une facture n'est jamais modifiée après émission : une erreur se corrige par un avoir (nouvelle ligne).
CREATE TABLE IF NOT EXISTS public.subscription_invoices (
    id                 uuid PRIMARY KEY,
    subscription_id    uuid                   NOT NULL REFERENCES public.school_subscriptions (id),
    number             character varying(30)  NOT NULL UNIQUE,   -- ex : DH-2026-000123 (séquentiel, sans trou)
    period_start       timestamp(6)           NOT NULL,
    period_end         timestamp(6)           NOT NULL,
    amount_fcfa        numeric(12, 0)         NOT NULL,
    status             character varying(20)  NOT NULL,
    provider           character varying(20),
    external_reference character varying(100) UNIQUE,            -- référence Campay : un paiement = une facture
    issued_at          timestamp(6)           NOT NULL,
    paid_at            timestamp(6),
    CONSTRAINT subscription_invoices_status_check CHECK (
        status IN ('ISSUED', 'PAID', 'FAILED', 'VOID', 'CREDIT_NOTE')
    ),
    CONSTRAINT subscription_invoices_amount_check CHECK (amount_fcfa >= 0 OR status = 'CREDIT_NOTE')
);

-- Les auto-écoles déjà validées démarrent leur essai aujourd'hui (équitable : personne ne perd de jours)
INSERT INTO public.school_subscriptions
    (id, registry_id, plan_code, status, trial_started_at, trial_ends_at, created_at, updated_at)
SELECT gen_random_uuid(), r.id, 'ESSENTIEL', 'TRIAL', now(), now() + interval '15 days', now(), now()
FROM public.driving_school_registry r
WHERE r.driving_school_status IN ('APPROVED', 'ACTIVE')
  AND NOT EXISTS (SELECT 1 FROM public.school_subscriptions s WHERE s.registry_id = r.id);
