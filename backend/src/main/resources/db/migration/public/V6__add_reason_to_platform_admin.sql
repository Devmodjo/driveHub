-- V6 — Ajout du champ reason dans platform_admin
ALTER TABLE public.platform_admin
ADD COLUMN IF NOT EXISTS reason character varying(500);