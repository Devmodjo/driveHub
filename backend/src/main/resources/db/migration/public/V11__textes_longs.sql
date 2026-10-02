-- ============================================================
-- V11 — Champs de texte long
-- ------------------------------------------------------------
-- La présentation d'une auto-école était limitée à 255 caractères (varchar(255)) :
-- au-delà, l'insertion échouait avec « value too long for type character varying(255) ».
-- Elle passe en TEXT (longueur libre). La longueur maximale acceptée est contrôlée dans le DTO
-- (DrivingSchoolRequestDto : 2000 caractères), avec un message d'erreur clair.
-- ============================================================
ALTER TABLE public.driving_school_registry ALTER COLUMN description TYPE text;

-- Ancienne table (données d'avant le multi-tenant, reprises par V9) : même correction
ALTER TABLE IF EXISTS public.driving_school ALTER COLUMN description TYPE text;
