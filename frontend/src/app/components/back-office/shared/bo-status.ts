/**
 * Libellés et couleurs communs aux écrans du back-office.
 *
 * Fonctions pures (sans état) : on les importe dans un composant et on les expose au gabarit,
 * par exemple : protected readonly schoolStatusLabel = schoolStatusLabel;
 */

/** Classes Tailwind des pastilles de statut (à combiner avec la classe globale .badge). */
const TONES = {
  green: 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
  amber: 'bg-amber-500/15 text-amber-700 dark:text-amber-300',
  red: 'bg-red-500/10 text-red-700 dark:text-red-300',
  gray: 'bg-black/5 text-black/60 dark:bg-white/10 dark:text-white/60',
  blue: 'bg-[#0070f3]/10 text-[#0070f3] dark:text-[#5ea8ff]',
} as const;

// ─── Auto-écoles (registres) ─────────────────────────────────────────

const SCHOOL_LABELS: Record<string, string> = {
  ACTIVE: 'Active',
  APPROVED: 'Approuvée',
  PENDING: 'En attente',
  REJECTED: 'Rejetée',
  SUSPENDED: 'Suspendue',
  INACTIVE: 'Inactive',
};

const SCHOOL_TONES: Record<string, string> = {
  ACTIVE: TONES.green,
  APPROVED: TONES.green,
  PENDING: TONES.amber,
  REJECTED: TONES.red,
  SUSPENDED: TONES.gray,
  INACTIVE: TONES.gray,
};

/** Libellé français du statut d'une auto-école (ex. PENDING → « En attente »). */
export function schoolStatusLabel(status: string): string {
  return SCHOOL_LABELS[status] ?? status;
}

/** Classes de la pastille de statut d'une auto-école. */
export function schoolStatusClass(status: string): string {
  return `badge ${SCHOOL_TONES[status] ?? TONES.gray}`;
}

// ─── Administrateurs ─────────────────────────────────────────────────

const ADMIN_LABELS: Record<string, string> = {
  ACTIVE: 'Actif',
  PENDING: 'En attente',
  INACTIVE: 'Inactif',
  SUSPENDED: 'Suspendu',
  EMAIL_PENDING: 'Email non vérifié',
};

const ADMIN_TONES: Record<string, string> = {
  ACTIVE: TONES.green,
  PENDING: TONES.amber,
  INACTIVE: TONES.gray,
  SUSPENDED: TONES.red,
  EMAIL_PENDING: TONES.blue,
};

/** Libellé français du statut d'un administrateur. */
export function adminStatusLabel(status: string): string {
  return ADMIN_LABELS[status] ?? status;
}

/** Classes de la pastille de statut d'un administrateur. */
export function adminStatusClass(status: string): string {
  return `badge ${ADMIN_TONES[status] ?? TONES.gray}`;
}

/** Libellé lisible d'un rôle d'administrateur. */
export function roleLabel(role: string | null | undefined): string {
  const labels: Record<string, string> = { ROOT: 'Root', SUPER_ADMIN: 'Super Admin', REVIEWER: 'Reviewer' };
  return role ? labels[role] ?? role : '';
}

/** Initiales d'un nom pour les avatars (ex. « Jean Dupont » → « JD »). */
export function initials(name: string | null | undefined): string {
  if (!name) {
    return '?';
  }
  return name
    .split(' ')
    .filter(Boolean)
    .map((word) => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}
