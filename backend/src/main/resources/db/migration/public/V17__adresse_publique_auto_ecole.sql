-- Adresse publique lisible de chaque auto-école (ex : /auto-ecoles/auto-ecole-le-volant-douala),
-- utilisée par la page publique et le référencement Google. Remplie par l'application
-- (création d'une demande, et au démarrage pour les auto-écoles existantes).
ALTER TABLE public.driving_school_registry ADD COLUMN IF NOT EXISTS slug character varying(120);
CREATE UNIQUE INDEX IF NOT EXISTS ux_driving_school_registry_slug ON public.driving_school_registry (slug);
