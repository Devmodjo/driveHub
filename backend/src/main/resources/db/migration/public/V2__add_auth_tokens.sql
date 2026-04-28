-- ============================================================
-- V2 — Tables de tokens pour verify-email et forgot-password
-- ============================================================

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    used boolean NOT NULL DEFAULT false,
    created_at timestamp without time zone DEFAULT now(),
    CONSTRAINT email_verification_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT email_verification_tokens_token_key UNIQUE (token),
    CONSTRAINT fk_evt_user FOREIGN KEY (user_id) REFERENCES _users(id)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    used boolean NOT NULL DEFAULT false,
    created_at timestamp without time zone DEFAULT now(),
    CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT password_reset_tokens_token_key UNIQUE (token),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES _users(id)
);