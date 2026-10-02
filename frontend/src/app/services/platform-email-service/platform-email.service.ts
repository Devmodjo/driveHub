import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AudienceCount, EmailAudience, EmailRecipientSuggestion, SendEmailRequest, SendEmailResponse, SentEmailPage,
} from '../../interfaces/PlatformEmail';
import { BASE_URL } from '../../utils/UTILS';

/**
 * Envoi d'emails depuis le back-office (/api/platform/emails).
 *
 * Seuls les administrateurs ROOT et SUPER_ADMIN peuvent envoyer : un REVIEWER reçoit une erreur 403.
 * Le jeton de l'administrateur est ajouté automatiquement par l'intercepteur (URL commençant par BASE_URL).
 */
@Injectable({ providedIn: 'root' })
export class PlatformEmailService {
  private readonly http = inject(HttpClient);
  private readonly url = `${BASE_URL}emails`;

  /** Envoie un email à une audience. Réponse 202 : l'envoi est accepté et traité en arrière-plan. */
  send(request: SendEmailRequest): Observable<SendEmailResponse> {
    return this.http.post<SendEmailResponse>(this.url, request);
  }

  /**
   * Nombre de destinataires d'une audience, à afficher avant l'envoi.
   * @param schoolId identifiant du registre de l'auto-école (seulement pour SCHOOL_MEMBERS)
   */
  audienceCount(audience: EmailAudience, schoolId?: string): Observable<AudienceCount> {
    let params = new HttpParams().set('audience', audience);
    if (schoolId) {
      params = params.set('schoolId', schoolId);
    }
    return this.http.get<AudienceCount>(`${this.url}/audience-count`, { params });
  }

  /** Recherche de destinataires (moniteurs, élèves, auto-écoles) : au moins 2 caractères, 20 résultats au plus. */
  searchRecipients(query: string): Observable<EmailRecipientSuggestion[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<EmailRecipientSuggestion[]>(`${this.url}/recipients`, { params });
  }

  /** Historique des envois, du plus récent au plus ancien. */
  history(page = 0, size = 10): Observable<SentEmailPage> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<SentEmailPage>(this.url, { params });
  }
}
