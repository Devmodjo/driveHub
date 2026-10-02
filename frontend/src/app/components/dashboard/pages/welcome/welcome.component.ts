import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import {
  DocumentRequirements, DocumentType, MyJoinRequest, MySchoolRegistry, SchoolRequest, UserDocument,
} from '../../../../interfaces/drivehub.models';
import { DocumentService } from '../../../../services/document-service/document.service';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { missingDocumentsText } from '../../../../shared/documents';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage, fieldErrorsOf } from '../../../../shared/http-error';
import { VALIDATION_DELAY } from '../../../../utils/UTILS';
import { ICONS } from '../../../../shared/icons';
import { DocumentUploadComponent } from '../../shared/document-upload.component';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';
import { WhyDocumentsComponent } from '../../shared/why-documents.component';

/**
 * Première étape après l'inscription, tant que l'utilisateur n'appartient à aucune auto-école
 * (pas de tenant dans son jeton) :
 *  - moniteur : demande de création de son auto-école (registre du schéma public), puis suivi
 *    de la validation par l'équipe DriveHub ;
 *  - élève (ou moniteur salarié) : demande d'adhésion à une auto-école du catalogue.
 *
 * Justificatifs : ils sont demandés ici, juste avant la demande (jamais à l'inscription).
 *  - moniteur : pièce d'identité + CAPEC, obligatoires avant d'envoyer la demande de création
 *    (le bouton « Envoyer la demande » reste inactif tant qu'il en manque) ;
 *  - élève : pièce d'identité, obligatoire avant de demander à rejoindre une auto-école.
 * La liste des pièces qui manquent vient du backend (GET /api/documents/requirements).
 * Après approbation, "Accéder à mon espace" récupère un nouveau jeton qui contient le tenant.
 */
@Component({
  selector: 'app-welcome',
  imports: [
    FormsModule, RouterLink, DatePipe, LucideDynamicIcon, PageHeaderComponent, StatusBadgeComponent,
    DocumentUploadComponent, WhyDocumentsComponent,
  ],
  template: `
    <app-page-header badge="Bienvenue" title="Configurons votre espace"
                     [subtitle]="session.isMonitor()
                       ? 'Inscrivez votre auto-école. Notre équipe vérifie chaque établissement avant son activation, sous ' + validationDelay + '.'
                       : 'Choisissez votre auto-école : elle validera votre inscription.'">
      <button class="btn-ghost" (click)="refreshAccess()" [disabled]="refreshing()">
        <svg [lucideIcon]="icons.RefreshCw" [size]="16" /> Accéder à mon espace
      </button>
    </app-page-header>

    @if (info()) { <div class="alert-success mb-6">{{ info() }}</div> }
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }


    <!-- Justificatifs : demandés juste avant la demande (création d'auto-école ou adhésion) -->
    @if (requirements(); as req) {
      <section class="premium-card rounded-[28px] p-5 sm:p-8 max-w-3xl mb-8" aria-labelledby="docs-title">
        <div class="flex flex-wrap items-start justify-between gap-3 mb-2">
          <h3 id="docs-title" class="text-xl font-bold tracking-tight">Vos justificatifs</h3>
          @if (req.missing.length === 0) {
            <span class="badge bg-emerald-500/10 text-emerald-700 dark:text-emerald-300">Complet</span>
          } @else {
            <span class="badge bg-amber-500/15 text-amber-700 dark:text-amber-300">{{ req.missing.length }} à fournir</span>
          }
        </div>
        <p class="text-sm text-black/60 dark:text-white/60 mb-3">
          @if (session.isMonitor() && !registry()) {
            Avant d'envoyer votre demande, ajoutez votre pièce d'identité et votre CAPEC. Une photo nette ou un scan suffit.
          } @else if (session.isMonitor()) {
            Les pièces jointes à votre demande. Si l'une d'elles est refusée, envoyez-en simplement une nouvelle.
          } @else {
            Avant de demander votre inscription dans une auto-école, ajoutez votre pièce d'identité. Une photo nette suffit.
          }
        </p>
        <app-why-documents [context]="session.isMonitor() && !joiningOnly() ? 'school' : 'join'" />
        <div class="grid grid-cols-1 gap-4 mt-5" [class]="req.required.length > 1 ? 'md:grid-cols-2' : ''">
          @for (type of req.required; track type) {
            <app-document-upload [type]="type" [document]="docOf(type)" (changed)="onDocumentChanged(type, $event)" />
          }
        </div>
      </section>
    }

    @if (session.isMonitor()) {
      @if (registry(); as reg) {
        <div class="premium-card rounded-[28px] p-8 max-w-2xl">
          <div class="flex items-center justify-between gap-4 mb-4">
            <h3 class="text-2xl font-bold tracking-tight">{{ reg.schoolName }}</h3>
            <app-status-badge [value]="reg.drivingSchoolStatus" />
          </div>
          <p class="text-black/60 dark:text-white/60 font-light mb-6">{{ reg.address }}, {{ reg.city }} - {{ reg.country }}</p>
          @switch (reg.drivingSchoolStatus) {
            @case ('PENDING') {
              <p class="text-sm">
                Votre demande est en cours d'examen par l'équipe DriveHub : elle sera traitée sous <strong>{{ validationDelay }}</strong>.
                Vous recevrez un email dès qu'elle sera validée.
              </p>
            }
            @case ('APPROVED') { <p class="text-sm">Votre auto-école est approuvée. Cliquez sur « Accéder à mon espace » pour commencer.</p> }
            @case ('ACTIVE') { <p class="text-sm">Votre auto-école est active. Cliquez sur « Accéder à mon espace » pour commencer.</p> }
            @default { <p class="text-sm">Votre auto-école n'est pas active. Contactez le support DriveHub.</p> }
          }
        </div>
      } @else if (!loading()) {
        @let e = fieldErrors();
        <!-- Limites (maxlength) identiques à celles du backend (DrivingSchoolRequestDto) -->
        <form class="premium-card rounded-[28px] p-5 sm:p-8 max-w-3xl space-y-5" (ngSubmit)="submitSchool()" novalidate>
          <h3 class="text-xl font-bold tracking-tight">Mon auto-école</h3>
          <p class="text-sm text-black/60 dark:text-white/60">
            Après l'envoi, notre équipe vérifie votre établissement sous {{ validationDelay }}. Vous serez prévenu par email.
          </p>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label class="field-label" for="name">Nom de l'établissement</label>
              <input id="name" class="field-input" [class.field-input-error]="e['name']" name="name" maxlength="150" required [(ngModel)]="school.name" />
              @if (e['name']) { <p class="field-error">{{ e['name'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="phone">Téléphone</label>
              <input id="phone" class="field-input" [class.field-input-error]="e['phoneNumber']" name="phone" type="tel" maxlength="20" placeholder="+237 6XX XX XX XX" required [(ngModel)]="school.phoneNumber" />
              @if (e['phoneNumber']) { <p class="field-error">{{ e['phoneNumber'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="email">Email</label>
              <input id="email" class="field-input" [class.field-input-error]="e['email']" type="email" name="email" maxlength="150" [(ngModel)]="school.email" />
              @if (e['email']) { <p class="field-error">{{ e['email'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="whatsapp">WhatsApp</label>
              <input id="whatsapp" class="field-input" [class.field-input-error]="e['whatsappNumber']" name="whatsapp" type="tel" maxlength="20" [(ngModel)]="school.whatsappNumber" />
              @if (e['whatsappNumber']) { <p class="field-error">{{ e['whatsappNumber'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="country">Pays</label>
              <input id="country" class="field-input" [class.field-input-error]="e['country']" name="country" maxlength="100" [(ngModel)]="school.country" />
              @if (e['country']) { <p class="field-error">{{ e['country'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="city">Ville</label>
              <input id="city" class="field-input" [class.field-input-error]="e['city']" name="city" maxlength="100" [(ngModel)]="school.city" />
              @if (e['city']) { <p class="field-error">{{ e['city'] }}</p> }
            </div>
            <div class="md:col-span-2">
              <label class="field-label" for="address">Adresse</label>
              <input id="address" class="field-input" [class.field-input-error]="e['address']" name="address" maxlength="255" required [(ngModel)]="school.address" />
              @if (e['address']) { <p class="field-error">{{ e['address'] }}</p> }
            </div>
            <div class="md:col-span-2">
              <label class="field-label" for="website">Site web</label>
              <input id="website" class="field-input" [class.field-input-error]="e['websiteUrl']" name="website" type="url" maxlength="255" placeholder="https://" [(ngModel)]="school.websiteUrl" />
              @if (e['websiteUrl']) { <p class="field-error">{{ e['websiteUrl'] }}</p> }
            </div>
            <div class="md:col-span-2">
              <div class="flex items-baseline justify-between">
                <label class="field-label" for="description">Présentation</label>
                <span class="text-xs text-black/40 dark:text-white/40">{{ school.description.length }} / {{ descriptionMax }}</span>
              </div>
              <textarea id="description" class="field-input min-h-28" [class.field-input-error]="e['description']" name="description"
                        [maxlength]="descriptionMax" [(ngModel)]="school.description"></textarea>
              @if (e['description']) { <p class="field-error">{{ e['description'] }}</p> }
            </div>
          </div>
          <div class="flex flex-col sm:flex-row sm:items-center gap-3">
            <button type="submit" class="btn-primary w-full sm:w-auto" [disabled]="sending() || !documentsReady()"
                    [attr.aria-describedby]="documentsReady() ? null : 'docs-hint'">
              {{ sending() ? 'Envoi...' : 'Envoyer la demande' }}
            </button>
            @if (!documentsReady()) {
              <p id="docs-hint" class="flex items-start gap-2 text-sm text-amber-700 dark:text-amber-300">
                <svg [lucideIcon]="icons.Info" [size]="16" class="shrink-0 mt-0.5" />
                <span>Ajoutez {{ missingText() }} (section « Vos justificatifs ») pour envoyer la demande.</span>
              </p>
            }
          </div>
        </form>

        <p class="mt-8 text-sm text-black/50 dark:text-white/50">
          Vous êtes moniteur salarié d'une auto-école déjà présente ?
          <a routerLink="/auto-ecoles" class="text-[#0070f3] font-semibold">Rejoignez-la depuis le catalogue</a>.
        </p>
      }
    } @else {
      <div class="mb-8 flex flex-col sm:flex-row sm:items-center gap-3">
        <a routerLink="/auto-ecoles" class="btn-primary"><svg [lucideIcon]="icons.Search" [size]="16" /> Trouver une auto-école</a>
        @if (!documentsReady()) {
          <p class="flex items-start gap-2 text-sm text-amber-700 dark:text-amber-300">
            <svg [lucideIcon]="icons.Info" [size]="16" class="shrink-0 mt-0.5" />
            <span>Ajoutez {{ missingText() }} avant d'envoyer votre demande d'inscription.</span>
          </p>
        }
      </div>
    }

    @if (joinRequests().length > 0) {
      <div class="premium-card rounded-[28px] p-6 mt-8 overflow-x-auto">
        <h3 class="text-lg font-bold mb-4">Mes demandes d'adhésion</h3>
        <table class="data-table">
          <thead><tr><th>Auto-école</th><th>Ville</th><th>Date</th><th>Statut</th></tr></thead>
          <tbody>
            @for (r of joinRequests(); track r.requestId) {
              <tr><td>{{ r.drivingSchoolName }}</td><td>{{ r.city }}</td><td>{{ r.requestedAt | date: 'dd/MM/yyyy' }}</td>
                <td><app-status-badge [value]="r.joinStatus" /></td></tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class WelcomeComponent {
  protected readonly icons = ICONS;
  protected readonly session = inject(SessionService);
  private readonly api = inject(SchoolApiService);
  private readonly documents = inject(DocumentService);
  private readonly router = inject(Router);

  protected readonly loading = signal(true);
  protected readonly registry = signal<MySchoolRegistry | null>(null);
  protected readonly joinRequests = signal<MyJoinRequest[]>([]);
  protected readonly sending = signal(false);
  protected readonly refreshing = signal(false);
  protected readonly info = signal('');
  /** Message d'erreur de chaque champ du formulaire, renvoyé par le backend. */
  protected readonly fieldErrors = signal<Record<string, string>>({});
  protected readonly validationDelay = VALIDATION_DELAY;
  /** Longueur maximale de la présentation (même valeur que le backend). */
  protected readonly descriptionMax = 2000;
  protected readonly error = signal('');

  /** Justificatifs déjà envoyés par l'utilisateur. */
  protected readonly myDocuments = signal<UserDocument[]>([]);
  /** Pièces demandées et pièces manquantes (null tant que la réponse n'est pas arrivée). */
  protected readonly requirements = signal<DocumentRequirements | null>(null);
  /** Vrai quand toutes les pièces demandées ont été envoyées. */
  protected readonly documentsReady = computed(() => (this.requirements()?.missing.length ?? 1) === 0);
  protected readonly missingText = computed(() => missingDocumentsText(this.requirements()?.missing ?? ['CNI']));
  /** Moniteur qui a déjà une demande d'adhésion : ses pièces sont vues par le responsable de l'auto-école. */
  protected readonly joiningOnly = computed(() => !this.registry() && this.joinRequests().length > 0);

  protected school: SchoolRequest = {
    name: '', email: '', country: 'Cameroun', city: '', phoneNumber: '', address: '',
    description: '', websiteUrl: '', whatsappNumber: '',
  };

  constructor() {
    this.load();
  }

  private load(): void {
    if (this.session.isMonitor()) {
      this.api.mySchool().subscribe({
        next: (reg) => { this.registry.set(reg); this.loading.set(false); },
        error: (err) => { this.error.set(errorMessage(err)); this.loading.set(false); },
      });
    } else {
      this.loading.set(false);
    }
    this.api.myJoinRequests().subscribe({ next: (list) => this.joinRequests.set(list ?? []) });
    this.documents.myDocuments().subscribe({ next: (docs) => this.myDocuments.set(docs ?? []) });
    this.loadRequirements();
  }

  private loadRequirements(): void {
    this.documents.requirements().subscribe({
      next: (req) => this.requirements.set(req),
      error: (err) => this.error.set(errorMessage(err, 'Impossible de charger la liste des justificatifs.')),
    });
  }

  /** Document déjà envoyé pour ce type (null s'il n'y en a pas). */
  protected docOf(type: DocumentType): UserDocument | null {
    return this.myDocuments().find((d) => d.type === type) ?? null;
  }

  /** Après un envoi ou une suppression : mise à jour de la liste, puis des pièces manquantes (backend). */
  protected onDocumentChanged(type: DocumentType, doc: UserDocument | null): void {
    const others = this.myDocuments().filter((d) => d.type !== type);
    this.myDocuments.set(doc ? [...others, doc] : others);
    this.loadRequirements();
  }

  protected submitSchool(): void {
    this.sending.set(true);
    this.error.set('');
    this.fieldErrors.set({});
    this.api.requestSchool(this.school).subscribe({
      next: (res) => { this.sending.set(false); this.info.set(res.message); this.load(); },
      error: (err) => {
        this.sending.set(false);
        this.error.set(errorMessage(err));
        this.fieldErrors.set(fieldErrorsOf(err));
      },
    });
  }

  /** Nouveau jeton : s'il contient un tenant, l'espace de l'auto-école s'ouvre. */
  protected refreshAccess(): void {
    this.refreshing.set(true);
    this.error.set('');
    this.info.set('');
    this.session.refresh().subscribe({
      next: () => {
        this.refreshing.set(false);
        if (this.session.tenant()) {
          this.router.navigate(['/dashboard/accueil']);
        } else {
          this.info.set(`Votre accès n'est pas encore ouvert : la demande est en cours de vérification (délai habituel : ${VALIDATION_DELAY}).`);
        }
      },
      error: (err) => { this.refreshing.set(false); this.error.set(errorMessage(err)); },
    });
  }
}
