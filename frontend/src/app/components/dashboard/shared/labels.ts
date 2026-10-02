/** Libellés français des valeurs d'énumération renvoyées par le backend. */
export const LABELS: Record<string, string> = {
  // Rôles
  MONITOR: 'Moniteur', STUDENT: 'Élève',
  // Statuts de demande
  PENDING: 'En attente', APPROVED: 'Approuvée', REJECTED: 'Rejetée', ACTIVE: 'Active', SUSPENDED: 'Suspendue',
  // Véhicules
  DISPOSABLE: 'Disponible', PANNE: 'En panne', MAINTENANCE: 'En maintenance',
  // Réservations
  CONDUITE: 'Leçon de conduite', RENDEZVOUS: 'Rendez-vous', ADMIN: 'Démarche administrative',
  CONFIRMED: 'Confirmée', CANCELLED: 'Annulée',
  // Paiements
  MOMO: 'MTN Mobile Money', OM: 'Orange Money', CASH: 'Espèces', INSCRIPTION: 'Inscription', EXAMS: 'Examen',
  VALIDATE: 'Validé', MANUAL: 'Caisse', SIMULATED: 'Simulation', CAMPAY: 'Campay',
  // Examens
  INSCRIT: 'Inscrit', REFUSE: 'Refusé',
  // Genre
  MALE: 'Homme', FEMALE: 'Femme',
};

export function label(value: string | null | undefined): string {
  return value ? LABELS[value] ?? value : '-';
}

/** Couleur du badge selon le statut. */
export function badgeClass(value: string | null | undefined): string {
  switch (value) {
    case 'APPROVED': case 'ACTIVE': case 'CONFIRMED': case 'VALIDATE': case 'INSCRIT': case 'DISPOSABLE':
      return 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300';
    case 'REJECTED': case 'CANCELLED': case 'REFUSE': case 'SUSPENDED': case 'PANNE':
      return 'bg-red-500/10 text-red-700 dark:text-red-300';
    case 'PENDING': case 'MAINTENANCE':
      return 'bg-amber-500/10 text-amber-700 dark:text-amber-300';
    default:
      return 'bg-black/5 dark:bg-white/10 text-black/70 dark:text-white/70';
  }
}

export const LICENSE_CATEGORIES = ['A', 'B', 'C', 'D', 'E', 'G'] as const;

/** "2026-10-02T14:30:00" → "02/10/2026 14:30" */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '-';
  const d = new Date(value);
  return isNaN(d.getTime()) ? value : d.toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' });
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return '-';
  const d = new Date(value);
  return isNaN(d.getTime()) ? value : d.toLocaleDateString('fr-FR');
}

export function formatAmount(value: number | null | undefined): string {
  return `${Number(value ?? 0).toLocaleString('fr-FR')} FCFA`;
}

/** Valeur d'un <input type="datetime-local"> ("2026-10-02T14:30") → format attendu par LocalDateTime. */
export function toLocalDateTime(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}
