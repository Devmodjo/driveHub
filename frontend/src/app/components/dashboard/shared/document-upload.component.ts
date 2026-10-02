import { HttpEventType } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, input, linkedSignal, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { Observable, Subscription } from 'rxjs';
import { DocumentType, UserDocument } from '../../../interfaces/drivehub.models';
import { DocumentService } from '../../../services/document-service/document.service';
import { DocumentViewerComponent } from '../../../shared/document-viewer.component';
import {
  ACCEPTED_DOCUMENT_ACCEPT, DOCUMENT_NUMBER_MAX, DOCUMENT_TYPE_LABELS, documentStatusClass, documentStatusLabel,
  formatFileSize, validateDocumentFile,
} from '../../../shared/documents';
import { errorMessage } from '../../../shared/http-error';
import { ICONS } from '../../../shared/icons';
import { formatDate } from './labels';

let nextId = 0;

/**
 * Emplacement d'un justificatif (une pièce d'identité OU un CAPEC) pour l'utilisateur connecté.
 *
 * Ce que fait le composant :
 *  - affiche l'état du document : À fournir, En vérification, Vérifié ou Refusé (avec le motif du refus) ;
 *  - propose un numéro de document facultatif, puis un bouton pour choisir le fichier
 *    (et, sur téléphone, un bouton pour prendre directement une photo) ;
 *  - vérifie le fichier avant l'envoi (format et 5 Mo maximum) pour prévenir tout de suite ;
 *  - envoie le fichier avec une barre de progression ;
 *  - permet de voir, remplacer ou supprimer le document (pas de modification une fois vérifié).
 *
 * Le parent reçoit (changed) après chaque envoi (le nouveau document) ou suppression (null),
 * pour mettre à jour sa liste et la liste des pièces manquantes.
 *
 * Utilisation :
 *   <app-document-upload type="CNI" [document]="docOf('CNI')" (changed)="onChanged('CNI', $event)" />
 */
@Component({
  selector: 'app-document-upload',
  imports: [FormsModule, LucideDynamicIcon, DocumentViewerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let doc = current();
    <div class="rounded-2xl border p-4 sm:p-5 bg-white dark:bg-[#0a0a0a] h-full flex flex-col"
         [class]="doc?.status === 'REJECTED' ? 'border-red-500/40' : 'border-black/10 dark:border-white/10'">
      <!-- En-tête : type de document + statut (la pastille passe sous le titre pour laisser la place au texte) -->
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 shrink-0 rounded-xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center">
          <svg [lucideIcon]="type() === 'CNI' ? icons.IdCard : icons.GraduationCap" [size]="20" />
        </div>
        <div class="min-w-0 grow">
          <p class="font-bold text-sm leading-snug text-black dark:text-white">{{ label() }}</p>
          <span class="badge mt-1.5" [class]="statusClass()">{{ statusLabel() }}</span>
        </div>
      </div>
      @if (doc) {
        <p class="mt-3 text-xs text-black/50 dark:text-white/50">
          <span class="block truncate" [title]="doc.fileName">{{ doc.fileName }}</span>
          {{ size(doc.sizeBytes) }} · envoyé le {{ date(doc.uploadedAt) }}@if (doc.documentNumberMasked) { · N° {{ doc.documentNumberMasked }}}
        </p>
      } @else {
        <p class="mt-3 text-xs text-black/50 dark:text-white/50">PDF ou photo (JPEG, PNG, WebP), 5 Mo maximum.</p>
      }

      @if (doc?.status === 'REJECTED') {
        <div class="mt-3 rounded-xl bg-red-500/10 px-3 py-2 text-sm text-red-700 dark:text-red-300">
          <strong>Motif du refus :</strong> {{ doc?.reviewComment || 'non précisé' }}. Envoyez un nouveau fichier.
        </div>
      }
      @if (doc?.status === 'VERIFIED') {
        <p class="mt-3 flex items-center gap-1.5 text-xs text-emerald-700 dark:text-emerald-300">
          <svg [lucideIcon]="icons.ShieldCheck" [size]="14" /> Document vérifié : il ne peut plus être modifié.
        </p>
      }

      <div class="grow"></div>

      @if (editable()) {
        <!-- Numéro facultatif, envoyé avec le prochain fichier -->
        <div class="mt-4">
          <label class="field-label" [for]="id + '-number'">Numéro du document (facultatif)</label>
          <input [id]="id + '-number'" class="field-input" [name]="id + '-number'" autocomplete="off"
                 [maxlength]="numberMax" [disabled]="uploading() || disabled()"
                 [ngModel]="number()" (ngModelChange)="number.set($event)" />
        </div>
      }

      @if (uploading()) {
        <div class="mt-4" role="status" aria-live="polite">
          <div class="flex items-center justify-between text-xs font-semibold text-black/60 dark:text-white/60 mb-1.5">
            <span class="flex items-center gap-1.5"><svg [lucideIcon]="icons.LoaderCircle" [size]="14" class="animate-spin" /> Envoi en cours...</span>
            <span>{{ progress() }} %</span>
          </div>
          <div class="h-2 rounded-full bg-black/5 dark:bg-white/10 overflow-hidden">
            <div class="h-full rounded-full bg-[#0070f3] transition-[width] duration-200" [style.width.%]="progress()"></div>
          </div>
        </div>
      }

      @if (error()) { <p class="field-error" role="alert">{{ error() }}</p> }
      @if (success()) { <p class="mt-2 text-sm text-emerald-700 dark:text-emerald-300" role="status">{{ success() }}</p> }

      <!-- Sélecteurs de fichier cachés : les boutons ci-dessous les ouvrent -->
      <input #picker type="file" class="hidden" [accept]="accept" (change)="onFile($event)" />
      <input #camera type="file" class="hidden" accept="image/*" capture="environment" (change)="onFile($event)" />

      @if (confirmDelete()) {
        <div class="mt-4 rounded-xl bg-black/[0.03] dark:bg-white/[0.05] p-3">
          <p class="text-sm font-semibold mb-2">Supprimer ce document ?</p>
          <div class="flex gap-2">
            <button type="button" class="btn-small bg-red-600 text-white hover:bg-red-700" [disabled]="deleting()" (click)="remove()">
              {{ deleting() ? 'Suppression...' : 'Supprimer' }}
            </button>
            <button type="button" class="btn-small hover:bg-black/5 dark:hover:bg-white/5" [disabled]="deleting()" (click)="confirmDelete.set(false)">Annuler</button>
          </div>
        </div>
      } @else {
        <!-- Mobile : boutons en grille de deux colonnes, faciles à toucher ; ensuite sur une ligne -->
        <div class="mt-4 grid grid-cols-2 gap-2 sm:flex sm:flex-wrap">
          @if (doc) {
            <button type="button" class="btn-small min-h-11 sm:min-h-9 bg-black/5 dark:bg-white/10" (click)="view()">
              <svg [lucideIcon]="icons.Eye" [size]="15" /> Voir
            </button>
          }
          @if (editable()) {
            <button type="button" class="btn-small min-h-11 sm:min-h-9 bg-[#0070f3] text-white hover:bg-[#0051af]" [disabled]="uploading() || disabled()" (click)="picker.click()">
              <svg [lucideIcon]="icons.Upload" [size]="15" />
              @if (doc) { Remplacer } @else { <span class="sm:hidden">Fichier</span><span class="hidden sm:inline">Choisir un fichier</span> }
            </button>
            <!-- Sur téléphone : ouvrir directement l'appareil photo (enveloppe : .btn-small fixe déjà display) -->
            <div class="sm:hidden">
              <button type="button" class="btn-small min-h-11 w-full bg-black/5 dark:bg-white/10" [disabled]="uploading() || disabled()" (click)="camera.click()" aria-label="Prendre une photo">
                <svg [lucideIcon]="icons.Camera" [size]="15" /> Photo
              </button>
            </div>
            @if (doc) {
              <button type="button" class="btn-small min-h-11 sm:min-h-9 text-red-600 dark:text-red-400 hover:bg-red-500/10" [disabled]="uploading()" (click)="confirmDelete.set(true)">
                <svg [lucideIcon]="icons.Trash" [size]="15" /> Supprimer
              </button>
            }
          }
        </div>
      }
    </div>

    @if (viewing(); as source) {
      <app-document-viewer [title]="label()" [fileName]="doc?.fileName ?? 'document'" [source]="source" (closed)="viewing.set(null)" />
    }
  `,
})
export class DocumentUploadComponent {
  protected readonly icons = ICONS;
  private readonly documents = inject(DocumentService);

  /** Type de justificatif de cet emplacement. */
  readonly type = input.required<DocumentType>();
  /** Document déjà envoyé (null s'il n'y en a pas encore). */
  readonly document = input<UserDocument | null>(null);
  /** Empêche l'envoi (ex : pendant le chargement de la page). */
  readonly disabled = input(false);
  /** Nouveau document après un envoi, ou null après une suppression. */
  readonly changed = output<UserDocument | null>();

  /** Identifiant unique, pour relier chaque <label> à son champ. */
  protected readonly id = `doc-upload-${++nextId}`;
  protected readonly accept = ACCEPTED_DOCUMENT_ACCEPT;
  protected readonly numberMax = DOCUMENT_NUMBER_MAX;
  protected readonly size = formatFileSize;
  protected readonly date = formatDate;

  /** Document affiché : celui du parent, remplacé localement après un envoi ou une suppression. */
  protected readonly current = linkedSignal(() => this.document());
  protected readonly number = signal('');
  protected readonly uploading = signal(false);
  protected readonly progress = signal(0);
  protected readonly deleting = signal(false);
  protected readonly confirmDelete = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly viewing = signal<Observable<Blob> | null>(null);

  protected readonly label = computed(() => DOCUMENT_TYPE_LABELS[this.type()]);
  protected readonly statusLabel = computed(() => documentStatusLabel(this.current()?.status));
  protected readonly statusClass = computed(() => documentStatusClass(this.current()?.status));
  /** Un document vérifié ne peut plus être remplacé ni supprimé. */
  protected readonly editable = computed(() => this.current()?.status !== 'VERIFIED');

  private upload$: Subscription | null = null;

  constructor() {
    // Si l'utilisateur quitte la page pendant l'envoi, on annule la requête
    inject(DestroyRef).onDestroy(() => this.upload$?.unsubscribe());
  }

  /** Fichier choisi (sélecteur ou appareil photo) : vérification, puis envoi immédiat. */
  protected onFile(event: Event): void {
    const field = event.target as HTMLInputElement;
    const file = field.files?.[0];
    field.value = '';   // permet de choisir à nouveau le même fichier après une erreur
    if (!file) return;
    this.send(file);
  }

  /** Vérifie puis envoie le fichier (public pour les tests). */
  send(file: File): void {
    this.error.set('');
    this.success.set('');
    const problem = validateDocumentFile(file);
    if (problem) {
      this.error.set(problem);
      return;
    }
    this.uploading.set(true);
    this.progress.set(0);
    this.upload$ = this.documents.upload(this.type(), file, this.number()).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.progress.set(Math.round((100 * event.loaded) / event.total));
        } else if (event.type === HttpEventType.Response && event.body) {
          this.uploading.set(false);
          this.number.set('');
          this.current.set(event.body);
          this.success.set('Document envoyé. Il sera vérifié avant la validation de votre demande.');
          this.changed.emit(event.body);
        }
      },
      error: (err) => {
        this.uploading.set(false);
        this.error.set(errorMessage(err, "L'envoi du document a échoué. Réessayez."));
      },
    });
  }

  protected view(): void {
    const doc = this.current();
    if (doc) this.viewing.set(this.documents.myFile(doc.id));
  }

  /** Supprime le document après confirmation (public pour les tests). */
  remove(): void {
    const doc = this.current();
    if (!doc) return;
    this.deleting.set(true);
    this.error.set('');
    this.success.set('');
    this.documents.deleteMine(doc.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.confirmDelete.set(false);
        this.current.set(null);
        this.changed.emit(null);
      },
      error: (err) => {
        this.deleting.set(false);
        this.confirmDelete.set(false);
        this.error.set(errorMessage(err, 'La suppression a échoué. Réessayez.'));
      },
    });
  }
}
