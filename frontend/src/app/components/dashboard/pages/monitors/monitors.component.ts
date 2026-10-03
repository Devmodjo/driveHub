import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { DocumentType, Monitor, MonitorCreateRequest } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import {
  ACCEPTED_DOCUMENT_ACCEPT, DOCUMENT_NUMBER_MAX, DOCUMENT_TYPE_LABELS, formatFileSize, validateDocumentFile,
} from '../../../../shared/documents';
import { errorMessage, fieldErrorsOf } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { SpamHintComponent } from '../../../../shared/spam-hint.component';
import { label } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { WhyDocumentsComponent } from '../../shared/why-documents.component';

/** Fichier choisi pour une pièce du formulaire d'ajout. */
interface PickedFile {
  file: File | null;
  number: string;
}

/**
 * Moniteurs de l'auto-école (/dashboard/moniteurs), pour le responsable.
 *
 *  - liste des moniteurs (GET /api/monitors) ;
 *  - « Ajouter un moniteur » : identité + pièce d'identité + CAPEC, envoyés en multipart
 *    (POST /api/monitors). Le backend crée le compte et envoie une invitation par email :
 *    le moniteur choisit lui-même son mot de passe (page /invitation).
 *
 * Les erreurs de saisie renvoyées par le backend (fieldErrors) s'affichent sous chaque champ.
 */
@Component({
  selector: 'app-monitors',
  imports: [FormsModule, LucideDynamicIcon, PageHeaderComponent, WhyDocumentsComponent, SpamHintComponent],
  template: `
    <app-page-header title="Moniteurs" subtitle="L'équipe pédagogique de votre auto-école.">
      @if (!formOpen()) {
        <button class="btn-primary" (click)="openForm()"><svg [lucideIcon]="icons.UserPlus" [size]="16" /> Ajouter un moniteur</button>
      }
    </app-page-header>

    @if (success()) {
      <!-- Invitation envoyée : on rappelle que l'email peut arriver dans les spams du moniteur -->
      <div class="mb-6">
        <div class="alert-success" role="status">{{ success() }}</div>
        <app-spam-hint audience="other" class="mt-2" />
      </div>
    }
    @if (error()) { <div class="alert-error mb-6" role="alert">{{ error() }}</div> }

    @if (formOpen()) {
      <form class="premium-card rounded-[28px] p-5 sm:p-8 mb-8 max-w-3xl space-y-6" (ngSubmit)="submit()" novalidate>
        <div>
          <h3 class="text-xl font-bold tracking-tight">Ajouter un moniteur</h3>
          <p class="mt-1 text-sm text-black/60 dark:text-white/60">
            Le moniteur recevra un email d'invitation pour choisir son mot de passe. Ses justificatifs sont considérés comme vérifiés par vous.
          </p>
        </div>

        <fieldset class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <legend class="sr-only">Identité et contact</legend>
          <div>
            <label class="field-label" for="m-firstname">Prénom</label>
            <input id="m-firstname" class="field-input" [class.field-input-error]="err('firstname')" name="firstname" maxlength="100" required autocomplete="off" [(ngModel)]="form.firstname" />
            @if (err('firstname'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
          <div>
            <label class="field-label" for="m-lastname">Nom</label>
            <input id="m-lastname" class="field-input" [class.field-input-error]="err('lastname')" name="lastname" maxlength="100" required autocomplete="off" [(ngModel)]="form.lastname" />
            @if (err('lastname'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
          <div>
            <label class="field-label" for="m-email">Email</label>
            <input id="m-email" type="email" class="field-input" [class.field-input-error]="err('email')" name="email" maxlength="150" required autocomplete="off" [(ngModel)]="form.email" />
            @if (err('email'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
          <div>
            <label class="field-label" for="m-phone">Téléphone</label>
            <input id="m-phone" type="tel" class="field-input" [class.field-input-error]="err('phoneNumber')" name="phoneNumber" maxlength="20" placeholder="+237 6XX XX XX XX" required [(ngModel)]="form.phoneNumber" />
            @if (err('phoneNumber'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
          <div>
            <label class="field-label" for="m-gender">Genre</label>
            <select id="m-gender" class="field-input" [class.field-input-error]="err('gender')" name="gender" [(ngModel)]="form.gender">
              <option value="MALE">Homme</option><option value="FEMALE">Femme</option>
            </select>
            @if (err('gender'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
          <div>
            <label class="field-label" for="m-birth">Date de naissance</label>
            <input id="m-birth" type="date" class="field-input dark:[color-scheme:dark]" [class.field-input-error]="err('dateOfBirth')" name="dateOfBirth" required [(ngModel)]="form.dateOfBirth" />
            @if (err('dateOfBirth'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
          <div>
            <label class="field-label" for="m-nationality">Nationalité</label>
            <input id="m-nationality" class="field-input" [class.field-input-error]="err('nationality')" name="nationality" maxlength="100" [(ngModel)]="form.nationality" />
            @if (err('nationality'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
          <div>
            <label class="field-label" for="m-city">Ville de résidence</label>
            <input id="m-city" class="field-input" [class.field-input-error]="err('residenceCity')" name="residenceCity" maxlength="100" [(ngModel)]="form.residenceCity" />
            @if (err('residenceCity'); as msg) { <p class="field-error">{{ msg }}</p> }
          </div>
        </fieldset>

        <!-- Justificatifs du moniteur -->
        <fieldset class="space-y-4">
          <legend class="text-base font-bold mb-1">Justificatifs</legend>
          <app-why-documents context="owner" />
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            @for (type of types; track type) {
              @let picked = files[type];
              <div class="rounded-2xl border p-4 space-y-3" [class]="err(fileField(type)) ? 'border-red-500/60' : 'border-black/10 dark:border-white/10'">
                <p class="font-bold text-sm leading-snug">{{ typeLabel(type) }}</p>
                <div>
                  <!-- Sélecteur natif caché (son texte suit la langue du navigateur) : un bouton en français l'ouvre -->
                  <p class="field-label" [id]="'m-' + type + '-file-label'">Fichier (PDF ou photo, 5 Mo max.)</p>
                  <input #fileInput [id]="'m-' + type + '-file'" type="file" class="hidden" [accept]="accept" (change)="pick(type, $event)" />
                  <div class="flex flex-wrap items-center gap-3">
                    <button type="button" class="btn-small min-h-10 bg-[#0070f3]/10 text-[#0070f3] hover:bg-[#0070f3]/20"
                            [attr.aria-describedby]="'m-' + type + '-file-label'" (click)="fileInput.click()">
                      <svg [lucideIcon]="icons.Upload" [size]="15" /> {{ picked.file ? 'Changer de fichier' : 'Choisir un fichier' }}
                    </button>
                    <span class="min-w-0 text-xs text-black/50 dark:text-white/50 break-all">
                      @if (picked.file) { {{ picked.file.name }} · {{ size(picked.file.size) }} } @else { Aucun fichier choisi }
                    </span>
                  </div>
                  @if (err(fileField(type)); as msg) { <p class="field-error">{{ msg }}</p> }
                </div>
                <div>
                  <label class="field-label" [for]="'m-' + type + '-number'">Numéro (facultatif)</label>
                  <input [id]="'m-' + type + '-number'" class="field-input" [name]="type + 'Number'" [maxlength]="numberMax" autocomplete="off"
                         [(ngModel)]="picked.number" />
                  @if (err(type === 'CNI' ? 'cniNumber' : 'capecNumber'); as msg) { <p class="field-error">{{ msg }}</p> }
                </div>
              </div>
            }
          </div>
        </fieldset>

        <div class="flex flex-col-reverse sm:flex-row gap-3">
          <button type="button" class="btn-ghost" [disabled]="sending()" (click)="closeForm()">Annuler</button>
          <button type="submit" class="btn-primary" [disabled]="sending()">
            @if (sending()) { <svg [lucideIcon]="icons.LoaderCircle" [size]="16" class="animate-spin" /> Envoi... } @else { <svg [lucideIcon]="icons.Send" [size]="16" /> Ajouter et inviter }
          </button>
        </div>
      </form>
    }

    <!-- Liste : cartes sur mobile, tableau à partir de md: -->
    <ul class="md:hidden space-y-3">
      @for (m of monitors(); track m.id) {
        <li class="premium-card rounded-[20px] p-4">
          <p class="font-bold">{{ m.firstname }} {{ m.lastname }}</p>
          <p class="text-sm text-black/50 dark:text-white/50 break-all">{{ m.email }}</p>
          <p class="mt-1 text-sm text-black/60 dark:text-white/60">{{ m.phoneNumber }}@if (m.residenceCity) { · {{ m.residenceCity }} }</p>
        </li>
      } @empty {
        <li class="premium-card rounded-[20px] p-6 text-center text-sm text-black/50 dark:text-white/50">{{ loading() ? 'Chargement...' : 'Aucun moniteur pour le moment.' }}</li>
      }
    </ul>
    <div class="hidden md:block premium-card rounded-[24px] p-4 overflow-x-auto">
      <table class="data-table">
        <thead><tr><th>Nom</th><th>Email</th><th>Téléphone</th><th>Genre</th><th>Ville</th></tr></thead>
        <tbody>
          @for (m of monitors(); track m.id) {
            <tr>
              <td class="font-semibold">{{ m.firstname }} {{ m.lastname }}</td><td>{{ m.email }}</td><td>{{ m.phoneNumber }}</td>
              <td>{{ label(m.gender) }}</td><td>{{ m.residenceCity || '-' }}</td>
            </tr>
          } @empty {
            <tr><td colspan="5" class="text-center text-black/50 dark:text-white/50">{{ loading() ? 'Chargement...' : 'Aucun moniteur pour le moment.' }}</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class MonitorsComponent {
  protected readonly icons = ICONS;
  protected readonly label = label;
  protected readonly size = formatFileSize;
  protected readonly accept = ACCEPTED_DOCUMENT_ACCEPT;
  protected readonly numberMax = DOCUMENT_NUMBER_MAX;
  protected readonly types: DocumentType[] = ['CNI', 'CAPEC'];
  private readonly api = inject(SchoolApiService);

  protected readonly monitors = signal<Monitor[]>([]);
  protected readonly loading = signal(true);
  protected readonly formOpen = signal(false);
  protected readonly sending = signal(false);
  protected readonly success = signal('');
  protected readonly error = signal('');
  /** Erreurs par champ : celles du backend et celles des contrôles faits avant l'envoi. */
  protected readonly fieldErrors = signal<Record<string, string>>({});

  protected form: MonitorCreateRequest = emptyForm();
  protected files: Record<DocumentType, PickedFile> = emptyFiles();

  constructor() {
    this.load();
  }

  private load(): void {
    this.api.monitors().subscribe({
      next: (list) => { this.monitors.set(list ?? []); this.loading.set(false); },
      error: (err) => { this.loading.set(false); this.error.set(errorMessage(err)); },
    });
  }

  protected openForm(): void {
    this.form = emptyForm();
    this.files = emptyFiles();
    this.fieldErrors.set({});
    this.success.set('');
    this.error.set('');
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
    this.fieldErrors.set({});
  }

  protected typeLabel(type: DocumentType): string {
    return DOCUMENT_TYPE_LABELS[type];
  }

  /** Nom du champ fichier attendu par le backend (cni, capec). */
  protected fileField(type: DocumentType): string {
    return type === 'CNI' ? 'cni' : 'capec';
  }

  /**
   * Message d'erreur d'un champ. Le backend peut nommer le champ « email » ou « monitor.email »
   * (partie JSON du multipart) : on accepte les deux.
   */
  protected err(field: string): string | null {
    const e = this.fieldErrors();
    return e[field] ?? e[`monitor.${field}`] ?? null;
  }

  /** Fichier choisi : vérifié tout de suite (format, taille), pour prévenir avant l'envoi. */
  protected pick(type: DocumentType, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    const errors = { ...this.fieldErrors() };
    delete errors[this.fileField(type)];
    if (file) {
      const problem = validateDocumentFile(file);
      if (problem) {
        errors[this.fileField(type)] = problem;
        input.value = '';
        this.files[type].file = null;
        this.fieldErrors.set(errors);
        return;
      }
    }
    this.files[type].file = file;
    this.fieldErrors.set(errors);
  }

  protected submit(): void {
    this.error.set('');
    this.success.set('');
    // Les deux fichiers sont obligatoires : on le signale avant d'appeler le backend
    const missing: Record<string, string> = {};
    for (const type of this.types) {
      if (!this.files[type].file) {
        missing[this.fileField(type)] = type === 'CNI' ? "Ajoutez la pièce d'identité du moniteur." : 'Ajoutez le CAPEC du moniteur.';
      }
    }
    if (Object.keys(missing).length > 0) {
      this.fieldErrors.set(missing);
      this.error.set('Les deux justificatifs sont obligatoires pour ajouter un moniteur.');
      return;
    }

    this.sending.set(true);
    this.fieldErrors.set({});
    const email = this.form.email.trim();
    this.api.addMonitor(
      { ...this.form, email },
      this.files.CNI.file as File, this.files.CAPEC.file as File,
      this.files.CNI.number, this.files.CAPEC.number,
    ).subscribe({
      next: () => {
        this.sending.set(false);
        this.formOpen.set(false);
        this.success.set(`Invitation envoyée à ${email}. Le moniteur pourra se connecter après avoir choisi son mot de passe.`);
        this.load();
      },
      error: (err) => {
        this.sending.set(false);
        this.error.set(errorMessage(err, "L'ajout du moniteur a échoué. Réessayez."));
        this.fieldErrors.set(fieldErrorsOf(err));
      },
    });
  }
}

function emptyForm(): MonitorCreateRequest {
  return {
    firstname: '', lastname: '', email: '', phoneNumber: '', gender: 'MALE',
    nationality: 'Camerounaise', residenceCity: '', dateOfBirth: '',
  };
}

function emptyFiles(): Record<DocumentType, PickedFile> {
  return { CNI: { file: null, number: '' }, CAPEC: { file: null, number: '' } };
}
