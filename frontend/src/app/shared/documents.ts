import { DocumentStatus, DocumentType } from '../interfaces/drivehub.models';

/**
 * Outils communs aux justificatifs (pièce d'identité, CAPEC), utilisés par le dashboard
 * et par le back-office. Fonctions pures : faciles à tester et à réutiliser.
 */

/** Types de fichiers acceptés par le backend (le contenu réel du fichier est aussi vérifié côté serveur). */
export const ACCEPTED_DOCUMENT_TYPES = ['application/pdf', 'image/jpeg', 'image/png', 'image/webp'] as const;

/** Valeur de l'attribut accept des <input type="file">. */
export const ACCEPTED_DOCUMENT_ACCEPT = ACCEPTED_DOCUMENT_TYPES.join(',');

/** Taille maximale d'un justificatif : 5 Mo (même limite que le backend). */
export const MAX_DOCUMENT_SIZE = 5 * 1024 * 1024;

/** Longueur maximale du numéro de document (même limite que le backend). */
export const DOCUMENT_NUMBER_MAX = 50;

/** Libellé complet de chaque type de justificatif. */
export const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  CNI: "Pièce d'identité (CNI ou passeport)",
  CAPEC: "CAPEC (Certificat d'aptitude à l'enseignement de la conduite)",
};

/** Libellé court (titres de liste, messages). */
export const DOCUMENT_TYPE_SHORT: Record<DocumentType, string> = {
  CNI: "Pièce d'identité",
  CAPEC: 'CAPEC',
};

/** Libellé du statut d'un justificatif. « null » signifie que le document n'a pas encore été envoyé. */
export function documentStatusLabel(status: DocumentStatus | null | undefined): string {
  switch (status) {
    case 'PENDING': return 'En vérification';
    case 'VERIFIED': return 'Vérifié';
    case 'REJECTED': return 'Refusé';
    default: return 'À fournir';
  }
}

/** Classes Tailwind de la pastille de statut (à combiner avec la classe globale .badge). */
export function documentStatusClass(status: DocumentStatus | null | undefined): string {
  switch (status) {
    case 'VERIFIED': return 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300';
    case 'REJECTED': return 'bg-red-500/10 text-red-700 dark:text-red-300';
    case 'PENDING': return 'bg-amber-500/15 text-amber-700 dark:text-amber-300';
    default: return 'bg-black/5 text-black/60 dark:bg-white/10 dark:text-white/60';
  }
}

/**
 * Vérifie un fichier avant l'envoi, pour prévenir l'utilisateur tout de suite
 * (le backend refait les mêmes contrôles). Renvoie le message d'erreur, ou null si le fichier convient.
 */
export function validateDocumentFile(file: File): string | null {
  if (file.size === 0) {
    return 'Ce fichier est vide. Choisissez un autre fichier.';
  }
  if (!(ACCEPTED_DOCUMENT_TYPES as readonly string[]).includes(file.type)) {
    return 'Format non accepté. Envoyez un PDF ou une photo (JPEG, PNG ou WebP).';
  }
  if (file.size > MAX_DOCUMENT_SIZE) {
    return `Fichier trop lourd (${formatFileSize(file.size)}). La taille maximale est de 5 Mo : réduisez la photo ou scannez en qualité standard.`;
  }
  return null;
}

/** 245112 → « 239 Ko » ; 2 400 000 → « 2,3 Mo ». */
export function formatFileSize(bytes: number | null | undefined): string {
  const value = Number(bytes ?? 0);
  if (value < 1024) return `${value} o`;
  if (value < 1024 * 1024) return `${Math.round(value / 1024)} Ko`;
  return `${(value / (1024 * 1024)).toLocaleString('fr-FR', { maximumFractionDigits: 1 })} Mo`;
}

/** Liste lisible des pièces manquantes : ['CNI', 'CAPEC'] → « votre pièce d'identité et votre CAPEC ». */
export function missingDocumentsText(missing: readonly DocumentType[]): string {
  const parts = missing.map((t) => (t === 'CNI' ? "votre pièce d'identité" : 'votre CAPEC'));
  return parts.length <= 1 ? parts.join('') : `${parts.slice(0, -1).join(', ')} et ${parts[parts.length - 1]}`;
}

/**
 * Le backend refuse une demande (création d'auto-école, adhésion) quand un justificatif manque :
 * réponse 400 avec un message qui parle de la pièce d'identité, du CAPEC ou des justificatifs.
 */
export function isMissingDocumentsError(status: number, message: string): boolean {
  return status === 400 && /justificatif|pi[eè]ce d'identit|CAPEC/i.test(message);
}
