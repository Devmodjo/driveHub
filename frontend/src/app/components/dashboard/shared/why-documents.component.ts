import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { ICONS } from '../../../shared/icons';

/** Qui vérifie les justificatifs : l'équipe DriveHub, le responsable de l'auto-école, ou le responsable qui les envoie. */
export type DocumentsContext = 'school' | 'join' | 'owner';

let nextId = 0;

/**
 * Lien discret « Pourquoi vous demande-t-on cela ? » qui déplie une courte explication rassurante.
 *
 * Accessibilité : c'est un vrai <button> ; aria-expanded indique aux lecteurs d'écran si le texte
 * est ouvert, et aria-controls relie le bouton au texte qu'il affiche.
 *
 * Utilisation : <app-why-documents context="school" />
 */
@Component({
  selector: 'app-why-documents',
  imports: [RouterLink, LucideDynamicIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="inline-flex items-center gap-1.5 text-sm font-semibold text-[#0070f3] hover:underline"
            [attr.aria-expanded]="open()" [attr.aria-controls]="panelId" (click)="open.set(!open())">
      <svg [lucideIcon]="icons.CircleHelp" [size]="16" />
      Pourquoi vous demande-t-on cela ?
      <svg [lucideIcon]="icons.ChevronDown" [size]="14" class="transition-transform" [class.rotate-180]="open()" />
    </button>
    @if (open()) {
      <div [id]="panelId" class="mt-3 rounded-2xl bg-[#0070f3]/[0.06] dark:bg-[#0070f3]/10 p-4 text-sm leading-relaxed text-black/70 dark:text-white/70 animate-expand">
        <p>
          DriveHub n'accueille que des <strong class="text-black dark:text-white">auto-écoles autorisées</strong> et des
          <strong class="text-black dark:text-white">moniteurs qualifiés</strong>. Vérifier l'identité de chacun, et le CAPEC des
          moniteurs, protège les élèves contre les auto-écoles clandestines.
        </p>
        <p class="mt-2 flex items-start gap-2">
          <svg [lucideIcon]="icons.Lock" [size]="16" class="shrink-0 mt-0.5 text-[#0070f3]" />
          <span>
            Vos fichiers sont chiffrés et ne sont jamais publics.
            @switch (context()) {
              @case ('school') { Seule l'équipe DriveHub chargée de valider les nouvelles auto-écoles peut les consulter. }
              @case ('join') { Seul le responsable de l'auto-école que vous souhaitez rejoindre peut les consulter, pour valider votre demande. }
              @default { Seules les personnes chargées de les vérifier peuvent les consulter. En les envoyant, vous confirmez les avoir contrôlés. }
            }
            Chaque consultation est enregistrée.
          </span>
        </p>
        <a routerLink="/confidentialite" target="_blank" class="mt-2 inline-block font-semibold text-[#0070f3] underline">
          Politique de confidentialité
        </a>
      </div>
    }
  `,
})
export class WhyDocumentsComponent {
  protected readonly icons = ICONS;
  readonly context = input<DocumentsContext>('school');
  protected readonly open = signal(false);
  /** Identifiant unique du texte (plusieurs liens peuvent exister sur la même page). */
  protected readonly panelId = `why-documents-${++nextId}`;
}
