import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { ICONS } from '../../../shared/icons';

/** Message de retour d'une action (succès ou erreur). */
export interface BoToastMessage {
  text: string;
  type: 'success' | 'error';
}

/**
 * Petit message flottant affiché après une action (« Auto-école approuvée », « Erreur... »).
 *
 * - Mobile : en bas de l'écran, sur toute la largeur (facile à lire avec le pouce).
 * - À partir de sm: : en haut à droite.
 * role="status" permet aux lecteurs d'écran d'annoncer le message.
 *
 * Utilisation : <bo-toast [message]="actionMessage()" />
 */
@Component({
  selector: 'bo-toast',
  imports: [LucideDynamicIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (message(); as msg) {
      <div role="status" aria-live="polite"
           class="fixed z-[70] inset-x-4 bottom-4 sm:inset-x-auto sm:bottom-auto sm:top-6 sm:right-6 sm:max-w-sm animate-expand">
        <div class="flex items-start gap-3 rounded-2xl px-4 py-3 text-sm font-semibold shadow-xl"
             [class]="msg.type === 'error' ? 'bg-red-600 text-white' : 'bg-black text-white dark:bg-white dark:text-black'">
          <svg [lucideIcon]="msg.type === 'error' ? icons.TriangleAlert : icons.CircleCheck" [size]="18" class="shrink-0 mt-0.5" />
          <span class="min-w-0 break-words">{{ msg.text }}</span>
        </div>
      </div>
    }
  `,
})
export class BoToastComponent {
  protected readonly icons = ICONS;
  readonly message = input<BoToastMessage | null>(null);
}
