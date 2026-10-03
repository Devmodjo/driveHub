import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { ICONS } from './icons';

/**
 * Pour qui est le conseil :
 *  - 'self'  : l'utilisateur attend lui-même l'email (inscription, mot de passe oublié...) ;
 *  - 'other' : l'email part vers une autre personne (ex : invitation d'un moniteur).
 */
export type SpamHintAudience = 'self' | 'other';

/**
 * Petit conseil discret affiché sous un message « email envoyé » :
 * les emails automatiques arrivent parfois dans les courriers indésirables (spams).
 *
 * Utilisation :
 *   <app-spam-hint />                      (l'utilisateur attend l'email)
 *   <app-spam-hint audience="other" />     (l'email est envoyé à quelqu'un d'autre)
 *
 * Pour l'espacer du message au-dessus, ajouter une classe sur la balise : <app-spam-hint class="mt-3" />
 */
@Component({
  selector: 'app-spam-hint',
  imports: [LucideDynamicIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  template: `
    <p class="flex items-start gap-2 text-xs leading-relaxed text-black/50 dark:text-white/50">
      <svg [lucideIcon]="icons.Inbox" [size]="14" class="shrink-0 mt-0.5" aria-hidden="true" />
      @if (audience() === 'other') {
        <span>Pas d'email reçu ? Demandez-lui de vérifier aussi ses courriers indésirables (spams).</span>
      } @else {
        <span>Vous ne le trouvez pas ? Regardez dans vos courriers indésirables (spams) et ajoutez l'expéditeur à vos contacts.</span>
      }
    </p>
  `,
})
export class SpamHintComponent {
  protected readonly icons = ICONS;
  readonly audience = input<SpamHintAudience>('self');
}
