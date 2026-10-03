-- ============================================================
-- V12 — Preuve du consentement à la politique de confidentialité
-- ------------------------------------------------------------
-- À l'inscription, l'utilisateur doit accepter la politique de confidentialité
-- (loi n° 2024/017 du 23 décembre 2024 : consentement libre, spécifique et éclairé).
-- On conserve la date et la version acceptées pour pouvoir le prouver.
-- Les comptes créés avant cette version restent NULL : ils devront accepter la politique
-- à leur prochaine connexion (évolution prévue côté frontend).
-- ============================================================
ALTER TABLE public._users ADD COLUMN IF NOT EXISTS privacy_policy_accepted_at timestamp(6);
ALTER TABLE public._users ADD COLUMN IF NOT EXISTS privacy_policy_version character varying(20);
