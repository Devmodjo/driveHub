import { DOCUMENT } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, input, output, signal } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { LucideDynamicIcon } from '@lucide/angular';
import { Observable } from 'rxjs';
import { errorMessage } from './http-error';
import { ICONS } from './icons';

/**
 * Visionneuse d'un justificatif (image ou PDF), affichée par-dessus la page.
 *
 * Le fichier arrive sous forme de Blob, téléchargé avec HttpClient (le jeton est donc ajouté par
 * l'intercepteur). On crée ensuite une adresse locale temporaire avec URL.createObjectURL :
 * elle n'existe que dans ce navigateur et elle est libérée (revokeObjectURL) à la fermeture.
 *
 * - Image : affichée directement.
 * - PDF : affiché dans un cadre sur ordinateur. Sur téléphone, les navigateurs affichent mal les PDF
 *   dans un cadre : on propose plutôt de l'ouvrir dans un nouvel onglet.
 *
 * Touche Échap : elle ferme uniquement la visionneuse, jamais la fenêtre ou le panneau qui l'a ouverte
 * (voir le constructeur).
 *
 * Utilisation :
 *   @if (viewing(); as v) {
 *     <app-document-viewer [title]="v.title" [fileName]="v.fileName" [source]="v.source" (closed)="viewing.set(null)" />
 *   }
 */
@Component({
  selector: 'app-document-viewer',
  imports: [LucideDynamicIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="fixed inset-0 z-[90] flex items-stretch sm:items-center justify-center bg-black/70 backdrop-blur-[2px] sm:p-6"
         (click)="closed.emit()">
      <div role="dialog" aria-modal="true" [attr.aria-label]="title()"
           class="flex w-full sm:max-w-4xl h-full sm:h-[88vh] flex-col bg-white dark:bg-[#0c0c0c] sm:rounded-3xl border border-black/5 dark:border-white/10 shadow-2xl overflow-hidden animate-expand"
           (click)="$event.stopPropagation()">
        <header class="flex items-center gap-3 px-4 sm:px-5 py-3 border-b border-black/5 dark:border-white/10">
          <div class="min-w-0 grow">
            <p class="font-bold text-sm sm:text-base text-black dark:text-white truncate">{{ title() }}</p>
            <p class="text-xs text-black/50 dark:text-white/50 truncate">{{ fileName() }}</p>
          </div>
          @if (url(); as u) {
            <a class="btn-small bg-black/5 dark:bg-white/10 text-black dark:text-white" [href]="u" target="_blank" rel="noopener"
               aria-label="Ouvrir dans un nouvel onglet">
              <svg [lucideIcon]="icons.ExternalLink" [size]="16" /> <span class="hidden sm:inline">Nouvel onglet</span>
            </a>
            <a class="btn-small bg-black/5 dark:bg-white/10 text-black dark:text-white" [href]="u" [attr.download]="fileName()"
               aria-label="Télécharger">
              <svg [lucideIcon]="icons.Download" [size]="16" /> <span class="hidden sm:inline">Télécharger</span>
            </a>
          }
          <button type="button" class="inline-flex items-center justify-center w-10 h-10 shrink-0 rounded-xl text-black/70 dark:text-white/70 hover:bg-black/5 dark:hover:bg-white/10"
                  (click)="closed.emit()" aria-label="Fermer">
            <svg [lucideIcon]="icons.X" [size]="20" />
          </button>
        </header>

        <div class="grow min-h-0 flex items-center justify-center bg-black/[0.03] dark:bg-white/[0.03]">
          @if (loading()) {
            <p class="flex items-center gap-2 text-sm text-black/60 dark:text-white/60">
              <svg [lucideIcon]="icons.LoaderCircle" [size]="18" class="animate-spin" /> Ouverture du document...
            </p>
          } @else if (error()) {
            <p class="alert-error m-5 max-w-md">{{ error() }}</p>
          } @else if (url(); as u) {
            @switch (kind()) {
              @case ('image') {
                <img [src]="u" [alt]="title()" class="max-w-full max-h-full object-contain p-2 sm:p-4" />
              }
              @case ('pdf') {
                <iframe [src]="pdfUrl()" [title]="title()" class="hidden sm:block w-full h-full border-0 bg-white"></iframe>
                <div class="sm:hidden p-6 text-center space-y-4">
                  <svg [lucideIcon]="icons.FileText" [size]="40" class="mx-auto text-[#0070f3]" />
                  <p class="text-sm text-black/60 dark:text-white/60">Ce document est un PDF. Ouvrez-le pour le lire en plein écran.</p>
                  <a class="btn-primary" [href]="u" target="_blank" rel="noopener">Ouvrir le PDF</a>
                </div>
              }
              @default {
                <div class="p-6 text-center space-y-4">
                  <p class="text-sm text-black/60 dark:text-white/60">Aperçu indisponible pour ce type de fichier.</p>
                  <a class="btn-primary" [href]="u" [attr.download]="fileName()">Télécharger</a>
                </div>
              }
            }
          }
        </div>
      </div>
    </div>
  `,
})
export class DocumentViewerComponent {
  protected readonly icons = ICONS;
  private readonly sanitizer = inject(DomSanitizer);

  readonly title = input.required<string>();
  readonly fileName = input('document');
  /** Téléchargement du fichier (ex : documentService.myFile(id)). Il est lancé à l'ouverture. */
  readonly source = input.required<Observable<Blob>>();
  readonly closed = output<void>();

  protected readonly loading = signal(true);
  protected readonly error = signal('');
  /** Adresse locale temporaire du fichier (blob:...). */
  protected readonly url = signal<string | null>(null);
  protected readonly kind = signal<'image' | 'pdf' | 'other'>('other');
  /** Un cadre (iframe) exige une adresse « de confiance » : on la marque comme sûre car elle vient de notre propre Blob. */
  protected readonly pdfUrl = computed(() => {
    const u = this.url();
    return u ? this.sanitizer.bypassSecurityTrustResourceUrl(u) : null;
  });

  constructor() {
    effect((onCleanup) => {
      const subscription = this.source().subscribe({
        next: (blob) => {
          this.revoke();
          this.kind.set(fileKind(blob.type, this.fileName()));
          this.url.set(URL.createObjectURL(blob));
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(errorMessage(err, "Impossible d'ouvrir ce document."));
        },
      });
      onCleanup(() => subscription.unsubscribe());
    });

    // Touche Échap. La fenêtre ou le panneau qui contient la visionneuse écoute aussi Échap sur
    // « document » (phase normale, dite de « bouillonnement »). On écoute ici en phase de « capture » :
    // notre écouteur passe donc AVANT les leurs, et stopPropagation() les empêche de recevoir la touche.
    // Résultat : Échap ferme seulement la visionneuse (l'élément le plus au-dessus), puis, si on
    // appuie à nouveau, la fenêtre en dessous.
    const page = inject(DOCUMENT);
    const onKeydown = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return;
      event.stopPropagation();
      this.closed.emit();
    };
    page.addEventListener('keydown', onKeydown, true);

    inject(DestroyRef).onDestroy(() => {
      page.removeEventListener('keydown', onKeydown, true);
      // Libère la mémoire occupée par le fichier quand la visionneuse se ferme
      this.revoke();
    });
  }

  private revoke(): void {
    const u = this.url();
    if (u) URL.revokeObjectURL(u);
  }
}

/** Type d'affichage selon le Content-Type renvoyé par le backend (ou, à défaut, l'extension du nom). */
export function fileKind(contentType: string, fileName: string): 'image' | 'pdf' | 'other' {
  const type = (contentType || '').toLowerCase();
  const name = (fileName || '').toLowerCase();
  if (type.startsWith('image/') || /\.(jpe?g|png|webp)$/.test(name)) return 'image';
  if (type === 'application/pdf' || name.endsWith('.pdf')) return 'pdf';
  return 'other';
}
