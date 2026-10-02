/**
 * Types de la page « Emails » du back-office (envoi d'emails aux auto-écoles, moniteurs et élèves).
 * Ils reprennent exactement le contrat de l'API /api/platform/emails : garder les deux synchronisés.
 */

/**
 * Destinataires possibles d'un envoi :
 *  - ALL_SCHOOLS    : toutes les auto-écoles (adresse email de l'auto-école) ;
 *  - ALL_MONITORS   : tous les moniteurs ;
 *  - ALL_STUDENTS   : tous les élèves ;
 *  - SCHOOL_MEMBERS : les membres d'une auto-école précise (schoolId obligatoire) ;
 *  - INDIVIDUAL     : une liste d'adresses choisies à la main (recipients obligatoire, 1 à 50).
 */
export type EmailAudience = 'ALL_SCHOOLS' | 'ALL_MONITORS' | 'ALL_STUDENTS' | 'SCHOOL_MEMBERS' | 'INDIVIDUAL';

/** Corps de POST /api/platform/emails. */
export interface SendEmailRequest {
  audience: EmailAudience;
  /** Identifiant du registre de l'auto-école, obligatoire pour SCHOOL_MEMBERS. */
  schoolId?: string;
  /** Adresses email (1 à 50), obligatoires pour INDIVIDUAL. */
  recipients?: string[];
  /** Objet : 3 à 150 caractères. */
  subject: string;
  /** Message en texte brut (10 à 5000 caractères) ; les retours à la ligne sont conservés. */
  message: string;
}

/** Réponse 202 de POST /api/platform/emails : l'envoi est accepté et part en arrière-plan. */
export interface SendEmailResponse {
  id: string;
  recipientCount: number;
  message: string;
}

/** Réponse de GET /api/platform/emails/audience-count. */
export interface AudienceCount {
  recipientCount: number;
}

/** Une suggestion de GET /api/platform/emails/recipients?q=... (20 au maximum). */
export interface EmailRecipientSuggestion {
  email: string;
  fullName: string;
  type: 'MONITOR' | 'STUDENT' | 'SCHOOL';
  schoolName: string | null;
}

/** Un envoi de l'historique (GET /api/platform/emails?page=&size=). */
export interface SentEmail {
  id: string;
  subject: string;
  message: string;
  audience: EmailAudience;
  schoolName: string | null;
  recipientCount: number;
  sentByEmail: string;
  /** Date et heure ISO de l'envoi. */
  sentAt: string;
}

/** Page de l'historique (format ApiPageResponse du backend). */
export interface SentEmailPage {
  content: SentEmail[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
