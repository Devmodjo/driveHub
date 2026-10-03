-- Stockage des justificatifs DANS la base PostgreSQL (storage.provider = DATABASE, valeur par défaut).
-- Le contenu est déjà chiffré (AES-256-GCM) par l'application avant d'arriver ici.
-- Avantage : aucun service de fichiers à configurer pour héberger DriveHub (la base suffit).
CREATE TABLE IF NOT EXISTS public.document_contents
(
    object_key character varying(255) PRIMARY KEY,
    content    bytea                  NOT NULL,
    created_at timestamp              NOT NULL DEFAULT now()
);
