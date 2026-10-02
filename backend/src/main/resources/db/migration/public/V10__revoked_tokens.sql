-- ============================================================
-- V10 — Déconnexion (logout)
-- ------------------------------------------------------------
-- Un JWT reste valide jusqu'à son expiration : pour le « déconnecter » côté serveur,
-- on enregistre son empreinte (SHA-256) ici. JwtAuthenticationFilter refuse tout jeton présent
-- dans cette table. Les lignes expirées sont supprimées chaque nuit (TokenRevocationService).
-- On stocke une empreinte et non le jeton : une fuite de la table ne permet pas de se connecter.
-- ============================================================
CREATE TABLE IF NOT EXISTS public.revoked_tokens (
    token_hash  VARCHAR(64) PRIMARY KEY,           -- SHA-256 du jeton, en hexadécimal
    expires_at  TIMESTAMP   NOT NULL,              -- date d'expiration du jeton (ensuite la ligne est inutile)
    revoked_at  TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_revoked_tokens_expires_at ON public.revoked_tokens (expires_at);
