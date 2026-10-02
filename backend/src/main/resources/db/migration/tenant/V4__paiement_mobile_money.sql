-- ============================================================
-- V4 (tenant) — Paiement Mobile Money (Campay)
-- ============================================================
ALTER TABLE payments ADD COLUMN IF NOT EXISTS provider character varying(20) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS phone_number character varying(30);
-- Référence de la transaction chez l'agrégateur (Campay "reference")
ALTER TABLE payments ADD COLUMN IF NOT EXISTS external_reference character varying(100);
-- Message utile à l'élève (code USSD, motif de refus...)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_message character varying(255);

ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_provider_check;
ALTER TABLE payments ADD CONSTRAINT payments_provider_check
    CHECK (provider IN ('MANUAL', 'SIMULATED', 'CAMPAY'));

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_external_reference
    ON payments (provider, external_reference) WHERE external_reference IS NOT NULL;
