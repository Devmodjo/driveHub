-- ============================================================
-- V14 — Justificatifs (pièce d'identité, CAPEC) et journal des consultations
-- ------------------------------------------------------------
-- Un justificatif par utilisateur et par type (CNI, CAPEC) : un nouvel envoi remplace l'ancien.
-- Les fichiers eux-mêmes ne sont PAS en base : ils sont chiffrés puis stockés sur Cloudflare R2
-- (ou dans un dossier local en développement). La base ne contient que :
--   * la clé de stockage (aléatoire, sans information personnelle) ;
--   * la clé de chiffrement du fichier, elle-même chiffrée par la clé maître (DATA_ENCRYPTION_KEY) ;
--   * le nom du fichier et le numéro du document, chiffrés (format « v1:... ») ;
--   * l'empreinte HMAC du numéro (repérer un même numéro utilisé par deux comptes, sans le déchiffrer).
-- ============================================================
CREATE TABLE IF NOT EXISTS public.user_documents (
    id                  uuid PRIMARY KEY,
    user_id             uuid                   NOT NULL REFERENCES public._users (id),
    type                character varying(20)  NOT NULL,
    status              character varying(20)  NOT NULL,
    object_key          character varying(255) NOT NULL UNIQUE,
    wrapped_data_key    text                   NOT NULL,
    content_type        character varying(50)  NOT NULL,
    size_bytes          bigint                 NOT NULL,
    sha256              character varying(64)  NOT NULL,   -- empreinte du fichier en clair (contrôle d'intégrité)
    file_name_enc       text,
    document_number_enc text,
    document_number_hash character varying(64),
    uploaded_at         timestamp(6)           NOT NULL,
    reviewed_at         timestamp(6),
    reviewed_by         character varying(150),
    review_comment      text,
    CONSTRAINT user_documents_type_check CHECK (type IN ('CNI', 'CAPEC')),
    CONSTRAINT user_documents_status_check CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    CONSTRAINT user_documents_user_type_unique UNIQUE (user_id, type)
);

CREATE INDEX IF NOT EXISTS idx_user_documents_number_hash ON public.user_documents (document_number_hash);

-- Qui a consulté quel justificatif, quand et depuis quel écran (traçabilité, loi n° 2024/017)
CREATE TABLE IF NOT EXISTS public.document_access_logs (
    id           uuid PRIMARY KEY,
    document_id  uuid                   NOT NULL,
    accessed_by  character varying(150) NOT NULL,
    actor_role   character varying(30)  NOT NULL,
    context      character varying(50)  NOT NULL,
    accessed_at  timestamp(6)           NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_document_access_logs_document ON public.document_access_logs (document_id, accessed_at DESC);
