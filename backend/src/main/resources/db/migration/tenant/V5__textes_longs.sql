-- ============================================================
-- V5 (schéma de chaque auto-école) — Champs de texte long
-- ------------------------------------------------------------
--  * driving_school.description : copie de la présentation saisie à la demande de création ;
--  * courses.content : le contenu d'un cours dépasse facilement 255 caractères ;
--  * payments.gateway_message : message renvoyé par l'agrégateur (Campay), de longueur variable.
-- ============================================================
ALTER TABLE driving_school ALTER COLUMN description TYPE text;
ALTER TABLE courses ALTER COLUMN content TYPE text;
ALTER TABLE payments ALTER COLUMN gateway_message TYPE text;
