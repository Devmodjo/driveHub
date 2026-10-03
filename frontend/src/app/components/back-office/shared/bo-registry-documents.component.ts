import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { Observable } from 'rxjs';
import { DocumentType, UserDocument } from '../../../interfaces/drivehub.models';
import { DocumentService } from '../../../services/document-service/document.service';
import { DocumentViewerComponent } from '../../../shared/document-viewer.component';
import {
  DOCUMENT_TYPE_LABELS, documentStatusClass, documentStatusLabel, formatFileSize,
} from '../../../shared/documents';
import { errorMessage } from '../../../shared/http-error';
import { ICONS } from '../../../shared/icons';
import { BoToastMessage } from './bo-toast.component';

/** Longueur minimale du motif de refus (il est envoyé par email au responsable). */
const COMMENT_MIN = 5;

/**
 * Bloc « Justificatifs du responsable » du back-office, affiché dans le détail d'une auto-école.
 *
 *  - liste la pièce d'identité et le CAPEC du fondateur (GET /api/platform/registries/{id}/documents) ;
 *  - « Voir » ouvre le fichier dans la visionneuse (consultation journalisée par le backend) ;
 *  - « Vérifier » valide le document ; « Refuser » ouvre un petit formulaire : le motif est
 *    obligatoire, car il est envoyé par email au responsable pour qu'il renvoie un bon document.
 *
 * Le parent affiche les messages de retour via (notify), avec son propre <bo-toast>.
 *
 * Utilisation : <bo-registry-documents [registryId]="school.id" (notify)="showMessage($event)" />
 */
@Component({
  selector: 'bo-registry-documents',
  imports: [FormsModule, LucideDynamicIcon, DocumentViewerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="rounded-2xl border border-black/[0.06] dark:border-white/10 p-4" aria-labelledby="bo-docs-title">
      <div class="flex items-center justify-between gap-3 mb-3">
        <h5 id="bo-docs-title" class="field-label mb-0!">Justificatifs du responsable</h5>
        @if (!loading() && !error()) {
          <span class="badge" [class]="allVerified() ? verifiedTone : pendingTone">{{ allVerified() ? 'Complet' : 'À vérifier' }}</span>
        }
      </div>

      @if (loading()) {
        <p class="py-4 text-sm text-black/50 dark:text-white/50">Chargement des justificatifs...</p>
      } @else if (error()) {
        <p class="text-sm text-red-600 dark:text-red-400">{{ error() }}</p>
      } @else {
        <ul class="space-y-3">
          @for (row of rows(); track row.type) {
            <li class="rounded-xl bg-black/[0.03] dark:bg-white/[0.04] p-3">
              <div class="flex items-start gap-3">
                <svg [lucideIcon]="row.type === 'CNI' ? icons.IdCard : icons.GraduationCap" [size]="18" class="shrink-0 mt-0.5 text-[#0070f3]" />
                <div class="min-w-0 grow">
                  <p class="text-sm font-bold leading-snug text-black dark:text-white">{{ typeLabel(row.type) }}</p>
                  @if (row.document; as d) {
                    <p class="text-xs text-black/50 dark:text-white/50 break-all">{{ d.fileName }} · {{ size(d.sizeBytes) }}</p>
                    @if (d.documentNumberMasked) { <p class="text-xs text-black/50 dark:text-white/50">N° {{ d.documentNumberMasked }}</p> }
                  } @else {
                    <p class="text-xs text-black/50 dark:text-white/50">Non fourni.</p>
                  }
                </div>
                <span class="badge shrink-0" [class]="statusClass(row.document?.status)">{{ statusLabel(row.document?.status) }}</span>
              </div>
              @if (row.document?.reviewComment) {
                <p class="mt-2 text-xs text-red-700 dark:text-red-300">Motif du refus : {{ row.document?.reviewComment }}</p>
              }

              @if (row.document; as d) {
                @if (rejecting() === d.id) {
                  <!-- Refus : motif obligatoire -->
                  <form class="mt-3 space-y-2" (ngSubmit)="reject(d)">
                    <label class="field-label mb-1!" [for]="'reason-' + d.id">Motif du refus (envoyé au responsable)</label>
                    <textarea [id]="'reason-' + d.id" name="reason" rows="2" maxlength="500" class="field-input text-sm!"
                              placeholder="Ex : photo floue, document expiré..." [(ngModel)]="comment"></textarea>
                    @if (commentError()) { <p class="bo-field-error">{{ commentError() }}</p> }
                    <div class="grid grid-cols-2 gap-2">
                      <button type="button" class="bo-btn bo-btn-outline" [disabled]="busy() === d.id" (click)="cancelReject()">Annuler</button>
                      <button type="submit" class="bo-btn bo-btn-red" [disabled]="busy() === d.id">
                        {{ busy() === d.id ? 'Envoi...' : 'Confirmer le refus' }}
                      </button>
                    </div>
                  </form>
                } @else {
                  <div class="mt-3 grid gap-2" [class]="d.status === 'PENDING' ? 'grid-cols-3' : 'grid-cols-1'">
                    <button type="button" class="bo-btn bo-btn-outline px-2!" (click)="view(d)">
                      <svg [lucideIcon]="icons.Eye" [size]="15" /> Voir
                    </button>
                    @if (d.status === 'PENDING') {
                      <button type="button" class="bo-btn bo-btn-green px-2!" [disabled]="busy() === d.id" (click)="verify(d)">Vérifier</button>
                      <button type="button" class="bo-btn bo-btn-outline px-2! text-red-600! dark:text-red-400!" [disabled]="busy() === d.id" (click)="startReject(d)">Refuser</button>
                    }
                  </div>
                }
              }
            </li>
          }
        </ul>
        <p class="mt-3 flex items-start gap-1.5 text-xs text-black/50 dark:text-white/50">
          <svg [lucideIcon]="icons.Lock" [size]="13" class="shrink-0 mt-0.5" /> Fichiers chiffrés. Chaque consultation est enregistrée.
        </p>
      }
    </section>

    @if (viewing(); as v) {
      <app-document-viewer [title]="typeLabel(v.type)" [fileName]="v.fileName" [source]="v.source" (closed)="viewing.set(null)" />
    }
  `,
})
export class BoRegistryDocumentsComponent {
  protected readonly icons = ICONS;
  protected readonly size = formatFileSize;
  protected readonly statusLabel = documentStatusLabel;
  protected readonly statusClass = documentStatusClass;
  protected readonly verifiedTone = documentStatusClass('VERIFIED');
  protected readonly pendingTone = documentStatusClass('PENDING');
  private readonly documents = inject(DocumentService);

  /** Identifiant du registre (demande de création) de l'auto-école. */
  readonly registryId = input.required<string>();
  /** Message à afficher par le parent après une action. */
  readonly notify = output<BoToastMessage>();

  protected readonly loading = signal(true);
  protected readonly error = signal('');
  private readonly list = signal<UserDocument[]>([]);
  protected readonly busy = signal<string | null>(null);
  /** Document dont le formulaire de refus est ouvert. */
  protected readonly rejecting = signal<string | null>(null);
  protected readonly commentError = signal('');
  protected comment = '';
  protected readonly viewing = signal<{ type: DocumentType; fileName: string; source: Observable<Blob> } | null>(null);

  /** Le fondateur doit fournir sa pièce d'identité et son CAPEC : une ligne par pièce, fournie ou non. */
  protected readonly rows = computed(() => {
    const docs = this.list();
    return (['CNI', 'CAPEC'] as DocumentType[]).map((type) => ({ type, document: docs.find((d) => d.type === type) ?? null }));
  });
  protected readonly allVerified = computed(() => this.rows().every((r) => r.document?.status === 'VERIFIED'));

  constructor() {
    // Rechargement automatique quand on ouvre le détail d'une autre auto-école
    effect(() => this.load(this.registryId()));
  }

  private load(registryId: string): void {
    this.loading.set(true);
    this.error.set('');
    this.documents.registryDocuments(registryId).subscribe({
      next: (docs) => { this.list.set(docs ?? []); this.loading.set(false); },
      error: (err) => { this.loading.set(false); this.error.set(errorMessage(err, 'Impossible de charger les justificatifs.')); },
    });
  }

  protected typeLabel(type: DocumentType): string {
    return DOCUMENT_TYPE_LABELS[type] ?? type;
  }

  protected view(doc: UserDocument): void {
    this.viewing.set({ type: doc.type, fileName: doc.fileName, source: this.documents.platformFile(doc.id) });
  }

  protected verify(doc: UserDocument): void {
    this.review(doc, 'VERIFIED', null, `${this.typeLabel(doc.type)} : document vérifié.`);
  }

  protected startReject(doc: UserDocument): void {
    this.comment = '';
    this.commentError.set('');
    this.rejecting.set(doc.id);
  }

  protected cancelReject(): void {
    this.rejecting.set(null);
    this.commentError.set('');
  }

  /** Refus : le motif est obligatoire (il est envoyé au responsable par email). */
  protected reject(doc: UserDocument): void {
    const comment = this.comment.trim();
    if (comment.length < COMMENT_MIN) {
      this.commentError.set(`Indiquez le motif du refus (au moins ${COMMENT_MIN} caractères) : il sera envoyé au responsable.`);
      return;
    }
    this.review(doc, 'REJECTED', comment, `${this.typeLabel(doc.type)} : document refusé, le responsable est prévenu par email.`);
  }

  private review(doc: UserDocument, status: 'VERIFIED' | 'REJECTED', comment: string | null, success: string): void {
    this.busy.set(doc.id);
    this.documents.review(doc.id, { status, comment }).subscribe({
      next: () => {
        this.busy.set(null);
        this.rejecting.set(null);
        this.notify.emit({ text: success, type: 'success' });
        this.load(this.registryId());
      },
      error: (err) => {
        this.busy.set(null);
        this.notify.emit({ text: errorMessage(err, "L'enregistrement de la décision a échoué."), type: 'error' });
      },
    });
  }
}
