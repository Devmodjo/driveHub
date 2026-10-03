import { HttpClient, HttpEvent } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DocumentRequirements, DocumentReview, DocumentType, UserDocument,
} from '../../interfaces/drivehub.models';
import { API_URL, BASE_URL } from '../../utils/UTILS';

/**
 * Justificatifs (pièce d'identité, CAPEC) : contrat dans docs/API-JUSTIFICATIFS.md.
 *
 * Sécurité : les fichiers ne sont jamais publics. On les télécharge toujours avec HttpClient
 * (responseType 'blob'), pour que l'intercepteur ajoute le jeton (et X-Tenant-ID si besoin).
 * Le composant les affiche ensuite avec URL.createObjectURL (voir DocumentViewerComponent) :
 * on n'ouvre jamais une adresse de fichier « brute », qui ne porterait pas le jeton.
 */
@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);

  // ─── Utilisateur connecté (seuls les moniteurs envoient des justificatifs) ───
  /** Mes justificatifs déjà envoyés. */
  myDocuments(): Observable<UserDocument[]> {
    return this.http.get<UserDocument[]>(`${API_URL}documents/me`);
  }

  /** Pièces demandées selon mon rôle et pièces qui manquent encore (listes vides pour un élève). */
  requirements(): Observable<DocumentRequirements> {
    return this.http.get<DocumentRequirements>(`${API_URL}documents/requirements`);
  }

  /**
   * Envoie (ou remplace) un justificatif. On observe les événements HTTP pour afficher
   * la progression de l'envoi : le dernier événement (type Response) contient le document créé.
   */
  upload(type: DocumentType, file: File, documentNumber?: string | null): Observable<HttpEvent<UserDocument>> {
    const form = new FormData();
    form.append('type', type);
    form.append('file', file, file.name);
    const number = documentNumber?.trim();
    if (number) {
      form.append('documentNumber', number);
    }
    return this.http.post<UserDocument>(`${API_URL}documents`, form, { reportProgress: true, observe: 'events' });
  }

  /** Contenu de mon document (fichier binaire). */
  myFile(documentId: string): Observable<Blob> {
    return this.http.get(`${API_URL}documents/${documentId}/file`, { responseType: 'blob' });
  }

  /** Supprime mon document (refusé par le backend, code 409, s'il est déjà vérifié). */
  deleteMine(documentId: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}documents/${documentId}`);
  }

  // ─── Responsable d'auto-école ──────────────────────────────────────
  /** Justificatifs du moniteur qui demande à rejoindre l'auto-école (liste vide pour un élève). */
  joinRequestDocuments(requestId: string): Observable<UserDocument[]> {
    return this.http.get<UserDocument[]>(`${API_URL}join-school/admin/${requestId}/documents`);
  }

  joinRequestFile(requestId: string, documentId: string): Observable<Blob> {
    return this.http.get(`${API_URL}join-school/admin/${requestId}/documents/${documentId}/file`, { responseType: 'blob' });
  }

  // ─── Back-office (jeton administrateur, ajouté par l'intercepteur pour /api/platform) ──
  /** Justificatifs du fondateur d'une auto-école (demande de création). */
  registryDocuments(registryId: string): Observable<UserDocument[]> {
    return this.http.get<UserDocument[]>(`${BASE_URL}registries/${registryId}/documents`);
  }

  /** Contenu d'un justificatif (chaque consultation est journalisée par le backend). */
  platformFile(documentId: string): Observable<Blob> {
    return this.http.get(`${BASE_URL}documents/${documentId}/file`, { responseType: 'blob' });
  }

  /** Vérifie ou refuse un justificatif (commentaire obligatoire en cas de refus). */
  review(documentId: string, review: DocumentReview): Observable<UserDocument> {
    return this.http.patch<UserDocument>(`${BASE_URL}documents/${documentId}/review`, review);
  }
}
