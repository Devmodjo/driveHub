import { ChangeDetectionStrategy, Component, computed, effect, input, output, signal } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { Observable } from 'rxjs';
import { DocumentType, UserDocument } from '../../../interfaces/drivehub.models';
import { DocumentViewerComponent } from '../../../shared/document-viewer.component';
import {
  DOCUMENT_TYPE_LABELS, documentStatusClass, documentStatusLabel, formatFileSize,
} from '../../../shared/documents';
import { errorMessage } from '../../../shared/http-error';
import { ICONS } from '../../../shared/icons';
import { formatDate } from './labels';

/** Une ligne de la liste : un document reçu, ou une pièce attendue mais pas encore fournie. */
interface Row {
  type: DocumentType;
  document: UserDocument | null;
}

/**
 * Fenêtre « Justificatifs de ... » pour le responsable d'auto-école (demandes d'adhésion des moniteurs).
 * Elle liste les pièces attendues (une pièce absente apparaît « À fournir ») et ouvre chaque fichier
 * dans la visionneuse.
 *
 * Le parent fournit le chargement de la liste et du fichier (routes de l'API propres à la demande) :
 *   <app-documents-dialog [title]="..." [documents]="docs$" [fileLoader]="loadFile" [expected]="['CNI', 'CAPEC']" (closed)="..." />
 */
@Component({
  selector: 'app-documents-dialog',
  imports: [LucideDynamicIcon, DocumentViewerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  // Échap ferme la fenêtre. Quand la visionneuse est ouverte par-dessus, c'est elle qui reçoit la touche
  // (elle l'arrête avant qu'elle n'arrive ici) : seule la visionneuse se ferme.
  host: { '(document:keydown.escape)': 'closed.emit()' },
  template: `
    <div class="fixed inset-0 z-[80] flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-[2px] sm:p-6"
         (click)="closed.emit()">
      <div role="dialog" aria-modal="true" [attr.aria-label]="title()"
           class="w-full sm:max-w-lg max-h-[90vh] overflow-y-auto rounded-t-3xl sm:rounded-3xl bg-white dark:bg-[#0c0c0c] border border-black/5 dark:border-white/10 p-5 sm:p-6 shadow-2xl animate-expand"
           (click)="$event.stopPropagation()">
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <h3 class="font-display text-xl font-black tracking-tight text-black dark:text-white">{{ title() }}</h3>
            @if (subtitle()) { <p class="mt-1 text-sm text-black/50 dark:text-white/50 break-words">{{ subtitle() }}</p> }
          </div>
          <button type="button" class="inline-flex items-center justify-center w-10 h-10 shrink-0 rounded-xl text-black/70 dark:text-white/70 hover:bg-black/5 dark:hover:bg-white/10"
                  (click)="closed.emit()" aria-label="Fermer">
            <svg [lucideIcon]="icons.X" [size]="20" />
          </button>
        </div>

        <div class="mt-5">
          @if (loading()) {
            <p class="py-8 text-center text-sm text-black/50 dark:text-white/50">Chargement des justificatifs...</p>
          } @else if (error()) {
            <p class="alert-error">{{ error() }}</p>
          } @else {
            <ul class="space-y-3">
              @for (row of rows(); track row.type + (row.document?.id ?? '')) {
                <li class="rounded-2xl border border-black/10 dark:border-white/10 p-4">
                  <div class="flex items-start gap-3">
                    <div class="w-10 h-10 shrink-0 rounded-xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center">
                      <svg [lucideIcon]="row.type === 'CNI' ? icons.IdCard : icons.GraduationCap" [size]="20" />
                    </div>
                    <div class="min-w-0 grow">
                      <p class="font-bold text-sm leading-snug text-black dark:text-white">{{ typeLabel(row.type) }}</p>
                      @if (row.document; as d) {
                        <p class="mt-0.5 text-xs text-black/50 dark:text-white/50 break-all">
                          {{ d.fileName }} · {{ size(d.sizeBytes) }} · envoyé le {{ date(d.uploadedAt) }}
                        </p>
                        @if (d.documentNumberMasked) { <p class="text-xs text-black/50 dark:text-white/50">N° {{ d.documentNumberMasked }}</p> }
                      } @else {
                        <p class="mt-0.5 text-xs text-black/50 dark:text-white/50">Non fourni par la personne.</p>
                      }
                    </div>
                    <span class="badge shrink-0" [class]="statusClass(row.document?.status)">{{ statusLabel(row.document?.status) }}</span>
                  </div>
                  @if (row.document?.reviewComment) {
                    <p class="mt-2 text-xs text-red-700 dark:text-red-300">Motif du refus : {{ row.document?.reviewComment }}</p>
                  }
                  @if (row.document; as d) {
                    <button type="button" class="mt-3 btn-small min-h-10 bg-black/5 dark:bg-white/10 w-full sm:w-auto" (click)="open(d)">
                      <svg [lucideIcon]="icons.Eye" [size]="15" /> Voir le document
                    </button>
                  }
                </li>
              } @empty {
                <li class="py-6 text-center text-sm text-black/50 dark:text-white/50">Aucun justificatif envoyé.</li>
              }
            </ul>
            <p class="mt-4 flex items-start gap-2 text-xs text-black/50 dark:text-white/50">
              <svg [lucideIcon]="icons.Lock" [size]="14" class="shrink-0 mt-0.5" />
              Documents confidentiels : consultez-les uniquement pour vérifier cette personne. Chaque consultation est enregistrée.
            </p>
          }
        </div>
      </div>
    </div>

    @if (viewing(); as v) {
      <app-document-viewer [title]="typeLabel(v.type)" [fileName]="v.fileName" [source]="v.source" (closed)="viewing.set(null)" />
    }
  `,
})
export class DocumentsDialogComponent {
  protected readonly icons = ICONS;
  protected readonly size = formatFileSize;
  protected readonly date = formatDate;
  protected readonly statusLabel = documentStatusLabel;
  protected readonly statusClass = documentStatusClass;

  readonly title = input.required<string>();
  readonly subtitle = input('');
  /** Chargement de la liste des justificatifs (lancé à l'ouverture). */
  readonly documents = input.required<Observable<UserDocument[]>>();
  /** Chargement du fichier d'un justificatif. */
  readonly fileLoader = input.required<(doc: UserDocument) => Observable<Blob>>();
  /** Pièces attendues pour cette personne (moniteur : pièce d'identité et CAPEC). */
  readonly expected = input<DocumentType[]>(['CNI', 'CAPEC']);
  readonly closed = output<void>();

  protected readonly loading = signal(true);
  protected readonly error = signal('');
  private readonly list = signal<UserDocument[]>([]);
  protected readonly viewing = signal<{ type: DocumentType; fileName: string; source: Observable<Blob> } | null>(null);

  /** Pièces attendues d'abord (fournies ou non), puis les éventuels autres documents. */
  protected readonly rows = computed<Row[]>(() => {
    const docs = this.list();
    const rows: Row[] = this.expected().map((type) => ({ type, document: docs.find((d) => d.type === type) ?? null }));
    for (const d of docs) {
      if (!this.expected().includes(d.type)) rows.push({ type: d.type, document: d });
    }
    return rows;
  });

  constructor() {
    effect((onCleanup) => {
      const subscription = this.documents().subscribe({
        next: (docs) => { this.list.set(docs ?? []); this.loading.set(false); },
        error: (err) => { this.loading.set(false); this.error.set(errorMessage(err, 'Impossible de charger les justificatifs.')); },
      });
      onCleanup(() => subscription.unsubscribe());
    });
  }

  protected typeLabel(type: DocumentType): string {
    return DOCUMENT_TYPE_LABELS[type] ?? type;
  }

  protected open(doc: UserDocument): void {
    this.viewing.set({ type: doc.type, fileName: doc.fileName, source: this.fileLoader()(doc) });
  }
}
