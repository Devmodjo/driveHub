import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { catchError, debounceTime, distinctUntilChanged, forkJoin, map, of, Subject, switchMap } from 'rxjs';
import { LucideDynamicIcon, LucideIcon } from '@lucide/angular';

import { AuthService } from '../../../../services/auth-service/auth.service';
import { DrivingSchoolService } from '../../../../services/school-service/driving-school.service';
import { PlatformEmailService } from '../../../../services/platform-email-service/platform-email.service';
import {
  EmailAudience, EmailRecipientSuggestion, SendEmailRequest, SentEmailPage,
} from '../../../../interfaces/PlatformEmail';
import { errorMessage, fieldErrorsOf } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { BoConfirmComponent } from '../../shared/bo-confirm.component';

/** Une option du choix des destinataires. */
interface AudienceOption {
  value: EmailAudience;
  label: string;
  hint: string;
  icon: LucideIcon;
}

/** Une adresse choisie pour un envoi INDIVIDUAL (affichée sous forme de « puce »). */
interface RecipientChip {
  email: string;
  /** Nom affiché si l'adresse vient de la recherche ; absent pour une adresse tapée à la main. */
  fullName?: string;
}

/** Auto-école proposée pour l'audience SCHOOL_MEMBERS. */
interface SchoolOption {
  id: string;
  name: string;
  city: string;
}

// Limites imposées par l'API (gardées identiques au backend)
export const SUBJECT_MIN = 3;
export const SUBJECT_MAX = 150;
export const MESSAGE_MIN = 10;
export const MESSAGE_MAX = 5000;
export const RECIPIENTS_MAX = 50;

/** Vérification simple d'une adresse email (la validation définitive est faite par le backend). */
const EMAIL_PATTERN = /^[^\s@,;]+@[^\s@,;]+\.[^\s@,;]{2,}$/;

/**
 * Page « Emails » du back-office (/backoffice/dashboard/emails).
 *
 * L'administrateur choisit une audience (toutes les auto-écoles, tous les moniteurs, tous les élèves,
 * les membres d'une auto-école ou des adresses choisies), rédige un objet et un message en texte brut,
 * voit le nombre de destinataires, confirme, puis consulte l'historique des envois.
 *
 * Seuls les rôles ROOT et SUPER_ADMIN peuvent envoyer (l'API répond 403 à un REVIEWER).
 */
@Component({
  selector: 'app-emails',
  imports: [DatePipe, LucideDynamicIcon, BoConfirmComponent],
  templateUrl: './emails.component.html',
})
export class EmailsComponent implements OnInit {
  private readonly emailService = inject(PlatformEmailService);
  private readonly schoolService = inject(DrivingSchoolService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly icons = ICONS;

  protected readonly limits = { SUBJECT_MAX, MESSAGE_MAX, RECIPIENTS_MAX };

  protected readonly audienceOptions: AudienceOption[] = [
    { value: 'ALL_SCHOOLS', label: 'Toutes les auto-écoles', hint: 'Adresse de contact de chaque auto-école', icon: ICONS.School },
    { value: 'ALL_MONITORS', label: 'Tous les moniteurs', hint: 'Tous les moniteurs inscrits', icon: ICONS.Users },
    { value: 'ALL_STUDENTS', label: 'Tous les élèves', hint: 'Tous les élèves inscrits', icon: ICONS.GraduationCap },
    { value: 'SCHOOL_MEMBERS', label: 'Membres d\'une auto-école', hint: 'Moniteurs et élèves d\'une auto-école', icon: ICONS.Building2 },
    { value: 'INDIVIDUAL', label: 'Destinataires choisis', hint: `Jusqu'à ${RECIPIENTS_MAX} adresses`, icon: ICONS.UserRound },
  ];

  // ─── Droits ────────────────────────────────────────────────────────
  /** Rôle de l'admin connecté (null tant qu'il n'est pas connu). */
  protected readonly role = signal<string | null>(null);
  /** Un REVIEWER peut consulter l'historique mais pas envoyer. */
  protected readonly canSend = computed(() => this.role() !== 'REVIEWER');

  // ─── Formulaire ────────────────────────────────────────────────────
  protected readonly audience = signal<EmailAudience>('ALL_SCHOOLS');
  protected readonly schoolId = signal('');
  protected readonly recipients = signal<RecipientChip[]>([]);
  protected readonly subject = signal('');
  protected readonly message = signal('');
  /** Passe à vrai au premier clic sur « Vérifier et envoyer » : on affiche alors les erreurs locales. */
  protected readonly submitted = signal(false);
  /** Erreurs par champ renvoyées par l'API (fieldErrors). */
  protected readonly serverErrors = signal<Record<string, string>>({});

  // ─── Auto-écoles (audience SCHOOL_MEMBERS) ─────────────────────────
  protected readonly schools = signal<SchoolOption[]>([]);
  protected readonly schoolsLoading = signal(false);
  protected readonly schoolsError = signal('');
  private schoolsLoaded = false;

  // ─── Recherche de destinataires (audience INDIVIDUAL) ──────────────
  protected readonly recipientQuery = signal('');
  protected readonly suggestions = signal<EmailRecipientSuggestion[]>([]);
  protected readonly searching = signal(false);
  /** Suggestion mise en surbrillance au clavier (-1 = aucune). */
  protected readonly activeSuggestion = signal(-1);
  protected readonly chipError = signal('');
  /** Le champ de recherche a le focus : la liste de suggestions n'est visible qu'à ce moment-là. */
  protected readonly recipientFocused = signal(false);
  private readonly search$ = new Subject<string>();

  // ─── Nombre de destinataires ───────────────────────────────────────
  protected readonly audienceCount = signal<number | null>(null);
  protected readonly countLoading = signal(false);
  protected readonly countError = signal('');
  private readonly count$ = new Subject<{ audience: EmailAudience; schoolId: string }>();

  /** Nombre de destinataires affiché : calculé localement pour INDIVIDUAL, demandé à l'API sinon. */
  protected readonly recipientCount = computed<number | null>(() =>
    this.audience() === 'INDIVIDUAL' ? this.recipients().length : this.audienceCount());

  // ─── Envoi ─────────────────────────────────────────────────────────
  protected readonly confirmOpen = signal(false);
  protected readonly sending = signal(false);
  protected readonly sendError = signal('');
  protected readonly sendSuccess = signal('');

  // ─── Historique ────────────────────────────────────────────────────
  protected readonly history = signal<SentEmailPage | null>(null);
  protected readonly historyPage = signal(0);
  protected readonly historyLoading = signal(false);
  protected readonly historyError = signal('');

  /** Erreurs de saisie détectées dans le navigateur, avant tout appel à l'API. */
  protected readonly localErrors = computed(() => {
    const errors: Record<string, string> = {};
    const subject = this.subject().trim();
    const message = this.message().trim();
    if (this.audience() === 'SCHOOL_MEMBERS' && !this.schoolId()) {
      errors['schoolId'] = 'Choisissez une auto-école.';
    }
    if (this.audience() === 'INDIVIDUAL' && this.recipients().length === 0) {
      errors['recipients'] = 'Ajoutez au moins un destinataire.';
    }
    if (subject.length < SUBJECT_MIN) {
      errors['subject'] = `L'objet doit contenir au moins ${SUBJECT_MIN} caractères.`;
    } else if (subject.length > SUBJECT_MAX) {
      errors['subject'] = `L'objet ne doit pas dépasser ${SUBJECT_MAX} caractères.`;
    }
    if (message.length < MESSAGE_MIN) {
      errors['message'] = `Le message doit contenir au moins ${MESSAGE_MIN} caractères.`;
    } else if (message.length > MESSAGE_MAX) {
      errors['message'] = `Le message ne doit pas dépasser ${MESSAGE_MAX} caractères.`;
    }
    return errors;
  });

  protected readonly isValid = computed(() => Object.keys(this.localErrors()).length === 0);

  /** Libellé de l'audience choisie (utilisé dans la confirmation). */
  protected readonly audienceLabel = computed(() =>
    this.audienceOptions.find((o) => o.value === this.audience())?.label ?? '');

  protected readonly selectedSchoolName = computed(() =>
    this.schools().find((s) => s.id === this.schoolId())?.name ?? '');

  /** Titre de la fenêtre de confirmation, ex. « Envoyer à 42 destinataires ? ». */
  protected readonly confirmTitle = computed(() => {
    const count = this.recipientCount();
    return count === null ? 'Envoyer ce message ?' : `Envoyer à ${this.plural(count)} ?`;
  });

  constructor() {
    // Recherche de destinataires : on attend 250 ms après la dernière frappe,
    // et une nouvelle frappe annule la recherche précédente (switchMap).
    this.search$.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((query) => {
        if (query.length < 2) {
          return of<EmailRecipientSuggestion[]>([]);
        }
        this.searching.set(true);
        return this.emailService.searchRecipients(query).pipe(catchError(() => of<EmailRecipientSuggestion[]>([])));
      }),
      takeUntilDestroyed(),
    ).subscribe((results) => {
      this.searching.set(false);
      // On masque les adresses déjà ajoutées
      const chosen = new Set(this.recipients().map((r) => r.email));
      this.suggestions.set(results.filter((r) => !chosen.has(r.email.toLowerCase())));
      this.activeSuggestion.set(-1);
    });

    // Nombre de destinataires d'une audience : seule la dernière demande compte (switchMap)
    this.count$.pipe(
      switchMap(({ audience, schoolId }) => {
        this.countLoading.set(true);
        this.countError.set('');
        return this.emailService.audienceCount(audience, schoolId || undefined).pipe(
          map((res) => ({ count: res.recipientCount, error: '' })),
          catchError((err) => of({ count: null, error: errorMessage(err, 'Nombre de destinataires indisponible.') })),
        );
      }),
      takeUntilDestroyed(),
    ).subscribe(({ count, error }) => {
      this.countLoading.set(false);
      this.audienceCount.set(count);
      this.countError.set(error);
    });
  }

  ngOnInit(): void {
    this.authService.getCurrentAdmin().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (profile) => this.role.set(profile.role),
      error: () => this.role.set(null),
    });
    this.refreshCount();
    this.loadHistory(0);
  }

  // ─── Audience ──────────────────────────────────────────────────────

  protected selectAudience(audience: EmailAudience): void {
    this.audience.set(audience);
    this.serverErrors.set({});
    if (audience === 'SCHOOL_MEMBERS') {
      this.loadSchools();
    }
    this.refreshCount();
  }

  protected selectSchool(id: string): void {
    this.schoolId.set(id);
    this.refreshCount();
  }

  /**
   * Demande le nombre de destinataires à l'API (sauf pour INDIVIDUAL, compté localement,
   * et pour SCHOOL_MEMBERS tant qu'aucune auto-école n'est choisie).
   */
  private refreshCount(): void {
    const audience = this.audience();
    if (audience === 'INDIVIDUAL' || (audience === 'SCHOOL_MEMBERS' && !this.schoolId())) {
      this.audienceCount.set(null);
      this.countError.set('');
      return;
    }
    this.count$.next({ audience, schoolId: audience === 'SCHOOL_MEMBERS' ? this.schoolId() : '' });
  }

  /** Charge une seule fois les auto-écoles actives et approuvées (liste des registres). */
  private loadSchools(): void {
    if (this.schoolsLoaded || this.schoolsLoading()) {
      return;
    }
    this.schoolsLoading.set(true);
    this.schoolsError.set('');
    forkJoin([
      this.schoolService.getAllRegistry(0, 200, 'ACTIVE'),
      this.schoolService.getAllRegistry(0, 200, 'APPROVED'),
    ]).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: ([active, approved]) => {
        const options = [...active.content, ...approved.content]
          .map((s) => ({ id: s.id, name: s.schoolName, city: s.city }))
          .sort((a, b) => a.name.localeCompare(b.name, 'fr'));
        this.schools.set(options);
        this.schoolsLoaded = true;
        this.schoolsLoading.set(false);
      },
      error: (err) => {
        this.schoolsLoading.set(false);
        this.schoolsError.set(errorMessage(err, 'Impossible de charger les auto-écoles.'));
      },
    });
  }

  // ─── Destinataires choisis (INDIVIDUAL) ────────────────────────────

  protected onRecipientInput(value: string): void {
    this.recipientQuery.set(value);
    this.chipError.set('');
    // Une adresse collée ou tapée suivie d'une virgule / d'un point-virgule est ajoutée directement
    if (/[,;\n]/.test(value)) {
      this.addFromText(value);
      return;
    }
    this.search$.next(value.trim());
  }

  protected onRecipientKeydown(event: KeyboardEvent): void {
    const list = this.suggestions();
    switch (event.key) {
      case 'ArrowDown':
        if (list.length) {
          event.preventDefault();
          this.activeSuggestion.set((this.activeSuggestion() + 1) % list.length);
        }
        break;
      case 'ArrowUp':
        if (list.length) {
          event.preventDefault();
          this.activeSuggestion.set((this.activeSuggestion() - 1 + list.length) % list.length);
        }
        break;
      case 'Enter': {
        // Entrée ne doit jamais soumettre le formulaire depuis ce champ
        event.preventDefault();
        const active = list[this.activeSuggestion()];
        if (active) {
          this.pickSuggestion(active);
        } else if (this.recipientQuery().trim()) {
          this.addFromText(this.recipientQuery());
        }
        break;
      }
      case 'Escape':
        this.suggestions.set([]);
        break;
      case 'Backspace':
        // Champ vide : Retour arrière retire la dernière puce
        if (!this.recipientQuery() && this.recipients().length) {
          this.recipients.update((list) => list.slice(0, -1));
        }
        break;
    }
  }

  protected pickSuggestion(suggestion: EmailRecipientSuggestion): void {
    if (this.addRecipient(suggestion.email, suggestion.fullName)) {
      this.clearRecipientInput();
    }
  }

  protected removeRecipient(email: string): void {
    this.recipients.update((list) => list.filter((r) => r.email !== email));
  }

  /** Ajoute une ou plusieurs adresses tapées ou collées (séparées par virgule, point-virgule, espace). */
  protected addFromText(text: string): void {
    const parts = text.split(/[\s,;]+/).map((p) => p.trim()).filter(Boolean);
    const invalid: string[] = [];
    for (const part of parts) {
      if (!this.addRecipient(part) && !EMAIL_PATTERN.test(part)) {
        invalid.push(part);
      }
    }
    if (invalid.length) {
      // On laisse l'adresse invalide dans le champ pour que l'admin la corrige
      this.recipientQuery.set(invalid.join(', '));
      this.chipError.set(`Adresse email invalide : ${invalid.join(', ')}`);
    } else {
      this.clearRecipientInput();
    }
  }

  /**
   * Ajoute une adresse si elle est valide, nouvelle et sous la limite.
   * @returns vrai si l'adresse a été ajoutée (ou était déjà présente)
   */
  private addRecipient(rawEmail: string, fullName?: string): boolean {
    const email = rawEmail.trim().toLowerCase();
    if (!EMAIL_PATTERN.test(email)) {
      return false;
    }
    if (this.recipients().some((r) => r.email === email)) {
      return true;
    }
    if (this.recipients().length >= RECIPIENTS_MAX) {
      this.chipError.set(`${RECIPIENTS_MAX} destinataires au maximum par envoi.`);
      return false;
    }
    this.recipients.update((list) => [...list, { email, fullName }]);
    // L'éventuelle erreur de l'API sur les destinataires n'a plus lieu d'être
    this.serverErrors.update((errors) => {
      const copy = { ...errors };
      delete copy['recipients'];
      return copy;
    });
    return true;
  }

  private clearRecipientInput(): void {
    this.recipientQuery.set('');
    this.suggestions.set([]);
    this.activeSuggestion.set(-1);
    this.search$.next('');
  }

  protected recipientTypeLabel(type: EmailRecipientSuggestion['type']): string {
    return { MONITOR: 'Moniteur', STUDENT: 'Élève', SCHOOL: 'Auto-école' }[type] ?? type;
  }

  // ─── Envoi ─────────────────────────────────────────────────────────

  /** Erreur à afficher sous un champ : celle de l'API en priorité, sinon l'erreur locale après un essai d'envoi. */
  protected errorFor(field: string): string {
    return this.serverErrors()[field] || (this.submitted() ? this.localErrors()[field] ?? '' : '');
  }

  /** Premier clic : on vérifie le formulaire puis on ouvre la confirmation. */
  protected review(): void {
    this.submitted.set(true);
    this.sendSuccess.set('');
    this.sendError.set('');
    if (!this.canSend() || !this.isValid()) {
      return;
    }
    this.confirmOpen.set(true);
  }

  /** Envoi confirmé : POST /api/platform/emails. */
  protected send(): void {
    const audience = this.audience();
    const request: SendEmailRequest = {
      audience,
      subject: this.subject().trim(),
      message: this.message().trim(),
    };
    if (audience === 'SCHOOL_MEMBERS') {
      request.schoolId = this.schoolId();
    }
    if (audience === 'INDIVIDUAL') {
      request.recipients = this.recipients().map((r) => r.email);
    }

    this.sending.set(true);
    this.serverErrors.set({});
    this.emailService.send(request).subscribe({
      next: (res) => {
        this.sending.set(false);
        this.confirmOpen.set(false);
        this.sendSuccess.set(`${res.message || 'Envoi en cours.'} (${this.plural(res.recipientCount)})`);
        this.resetComposer();
        this.loadHistory(0);
      },
      error: (err) => {
        this.sending.set(false);
        this.confirmOpen.set(false);
        this.serverErrors.set(fieldErrorsOf(err));
        const fallback = err?.status === 403
          ? 'Seuls les administrateurs ROOT et SUPER_ADMIN peuvent envoyer des emails.'
          : 'L\'envoi a échoué. Vérifiez le formulaire et réessayez.';
        this.sendError.set(errorMessage(err, fallback));
      },
    });
  }

  /** Vide l'objet, le message et les puces (l'audience choisie est conservée). */
  private resetComposer(): void {
    this.subject.set('');
    this.message.set('');
    this.recipients.set([]);
    this.clearRecipientInput();
    this.submitted.set(false);
  }

  // ─── Historique ────────────────────────────────────────────────────

  protected loadHistory(page: number): void {
    this.historyLoading.set(true);
    this.historyError.set('');
    this.emailService.history(page, 10).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (res) => {
        this.history.set(res);
        this.historyPage.set(page);
        this.historyLoading.set(false);
      },
      error: (err) => {
        this.historyLoading.set(false);
        this.historyError.set(errorMessage(err, 'Impossible de charger l\'historique des envois.'));
      },
    });
  }

  /** Libellé lisible de l'audience d'un envoi de l'historique. */
  protected historyAudienceLabel(audience: EmailAudience): string {
    return this.audienceOptions.find((o) => o.value === audience)?.label ?? audience;
  }

  /** « 1 destinataire », « 12 destinataires ». */
  protected plural(count: number): string {
    return `${count} destinataire${count > 1 ? 's' : ''}`;
  }
}
