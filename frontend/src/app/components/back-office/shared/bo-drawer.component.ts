import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { ICONS } from '../../../shared/icons';

/**
 * Volet latéral (détail d'une auto-école, d'un admin, profil...).
 *
 * - Mobile : il occupe tout l'écran, pour laisser de la place au contenu.
 * - À partir de sm: : panneau de 28rem collé à droite, au-dessus d'un voile sombre.
 * Un clic sur le voile, le bouton de fermeture ou la touche Échap émet (closed).
 *
 * Utilisation :
 *   @if (open()) {
 *     <bo-drawer title="Détails" (closed)="open.set(false)"> ...contenu... </bo-drawer>
 *   }
 */
@Component({
  selector: 'bo-drawer',
  imports: [LucideDynamicIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'closed.emit()' },
  template: `
    <div class="fixed inset-0 z-[60] bg-black/50 backdrop-blur-[2px]" (click)="closed.emit()" aria-hidden="true"></div>
    <aside role="dialog" aria-modal="true" [attr.aria-label]="title()"
           class="fixed inset-y-0 right-0 z-[61] flex w-full sm:max-w-md flex-col bg-white dark:bg-[#0c0c0c] sm:border-l border-black/5 dark:border-white/10 shadow-2xl animate-expand">
      <header class="flex items-center justify-between gap-3 px-5 py-3 border-b border-black/5 dark:border-white/10">
        <h3 class="bo-section-title truncate">{{ title() }}</h3>
        <button type="button" class="bo-icon-btn" (click)="closed.emit()" aria-label="Fermer">
          <svg [lucideIcon]="icons.X" [size]="20" />
        </button>
      </header>
      <div class="grow overflow-y-auto px-5 py-5">
        <ng-content />
      </div>
    </aside>
  `,
})
export class BoDrawerComponent {
  protected readonly icons = ICONS;
  readonly title = input.required<string>();
  readonly closed = output<void>();
}
