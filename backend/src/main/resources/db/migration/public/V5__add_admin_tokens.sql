CREATE TABLE IF NOT EXISTS public.admin_email_verification_tokens (
    id uuid NOT NULL,
    admin_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    expires_at timestamp NOT NULL,
    used boolean NOT NULL DEFAULT false,
    created_at timestamp DEFAULT now(),
    CONSTRAINT admin_evt_pkey PRIMARY KEY (id),
    CONSTRAINT admin_evt_token_unique UNIQUE (token),
    CONSTRAINT admin_evt_admin_fk
        FOREIGN KEY (admin_id) REFERENCES public.platform_admin(id)
);

CREATE TABLE IF NOT EXISTS public.admin_password_reset_tokens (
    id uuid NOT NULL,
    admin_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    expires_at timestamp NOT NULL,
    used boolean NOT NULL DEFAULT false,
    created_at timestamp DEFAULT now(),
    CONSTRAINT admin_prt_pkey PRIMARY KEY (id),
    CONSTRAINT admin_prt_token_unique UNIQUE (token),
    CONSTRAINT admin_prt_admin_fk
        FOREIGN KEY (admin_id) REFERENCES public.platform_admin(id)
);