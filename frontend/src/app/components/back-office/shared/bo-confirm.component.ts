import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

/**
 * Fenêtre de confirmation, qui remplace le confirm() du navigateur (illisible sur mobile et impossible à styliser).
 *
 * - Mobile : feuille qui monte du bas de l'écran, boutons empilés et larges (faciles à toucher).
 * - À partir de sm: : petite fenêtre centrée, boutons côte à côte.
 *
 * Utilisation :
 *   @if (pending()) {
 *     <bo-confirm title="Supprimer cette auto-école ?" message="Action irréversible." tone="danger"
 *                 confirmLabel="Supprimer" (confirmed)="..." (cancelled)="pending.set(null)" />
 *   }
 * Un contenu supplémentaire (résumé d'un email...) peut être placé entre les balises.
 */
@Component({
  selector: 'bo-confirm',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'busy() || cancelled.emit()' },
  template: `
    <div class="fixed inset-0 z-[80] flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-[2px] sm:p-6"
         (click)="busy() || cancelled.emit()">
      <div role="alertdialog" aria-modal="true" [attr.aria-label]="title()"
           class="w-full sm:max-w-md rounded-t-3xl sm:rounded-3xl bg-white dark:bg-[#0c0c0c] border border-black/5 dark:border-white/10 p-6 shadow-2xl animate-expand"
           (click)="$event.stopPropagation()">
        <h3 class="font-display text-xl font-black tracking-tight text-black dark:text-white">{{ title() }}</h3>
        @if (message()) {
          <p class="mt-2 text-sm text-black/60 dark:text-white/60 leading-relaxed">{{ message() }}</p>
        }
        <ng-content />
        <div class="mt-6 flex flex-col-reverse sm:flex-row sm:justify-end gap-2">
          <button type="button" class="bo-btn bo-btn-outline" [disabled]="busy()" (click)="cancelled.emit()">
            {{ cancelLabel() }}
          </button>
          <button type="button" class="bo-btn" [class]="toneClass()" [disabled]="busy()" (click)="confirmed.emit()">
            {{ busy() ? 'Veuillez patienter...' : confirmLabel() }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class BoConfirmComponent {
  readonly title = input.required<string>();
  readonly message = input('');
  readonly confirmLabel = input('Confirmer');
  readonly cancelLabel = input('Annuler');
  /** Couleur du bouton de confirmation : danger (rouge), warning (ambre) ou primary (bleu). */
  readonly tone = input<'danger' | 'warning' | 'primary'>('primary');
  /** Pendant l'appel à l'API : boutons désactivés, fermeture impossible. */
  readonly busy = input(false);

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  protected toneClass(): string {
    const tones = { danger: 'bo-btn-red', warning: 'bo-btn-amber', primary: 'bo-btn-blue' };
    return tones[this.tone()];
  }
}
