import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { MyJoinRequest, MySchoolRegistry, SchoolRequest } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

/**
 * Première étape après l'inscription, tant que l'utilisateur n'appartient à aucune auto-école
 * (pas de tenant dans son jeton) :
 *  - moniteur : demande de création de son auto-école (registre du schéma public), puis suivi
 *    de la validation par l'équipe DriveHub ;
 *  - élève (ou moniteur salarié) : demande d'adhésion à une auto-école du catalogue.
 * Après approbation, "Accéder à mon espace" récupère un nouveau jeton qui contient le tenant.
 */
@Component({
  selector: 'app-welcome',
  imports: [FormsModule, RouterLink, DatePipe, LucideAngularModule, PageHeaderComponent, StatusBadgeComponent],
  template: `
    <app-page-header badge="Bienvenue" title="Configurons votre espace"
                     [subtitle]="session.isMonitor()
                       ? 'Inscrivez votre auto-école. Notre équipe vérifie chaque établissement avant son activation.'
                       : 'Choisissez votre auto-école : elle validera votre inscription.'">
      <button class="btn-ghost" (click)="refreshAccess()" [disabled]="refreshing()">
        <lucide-icon [img]="icons.RefreshCw" [size]="16" /> Accéder à mon espace
      </button>
    </app-page-header>

    @if (info()) { <div class="alert-success mb-6">{{ info() }}</div> }
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (session.isMonitor()) {
      @if (registry(); as reg) {
        <div class="premium-card rounded-[28px] p-8 max-w-2xl">
          <div class="flex items-center justify-between gap-4 mb-4">
            <h3 class="text-2xl font-bold tracking-tight">{{ reg.schoolName }}</h3>
            <app-status-badge [value]="reg.drivingSchoolStatus" />
          </div>
          <p class="text-black/60 dark:text-white/60 font-light mb-6">{{ reg.address }}, {{ reg.city }} - {{ reg.country }}</p>
          @switch (reg.drivingSchoolStatus) {
            @case ('PENDING') { <p class="text-sm">Votre demande est en cours d'examen par l'équipe DriveHub. Vous recevrez un email dès qu'elle sera traitée.</p> }
            @case ('APPROVED') { <p class="text-sm">Votre auto-école est approuvée. Cliquez sur « Accéder à mon espace » pour commencer.</p> }
            @case ('ACTIVE') { <p class="text-sm">Votre auto-école est active. Cliquez sur « Accéder à mon espace » pour commencer.</p> }
            @default { <p class="text-sm">Votre auto-école n'est pas active. Contactez le support DriveHub.</p> }
          }
        </div>
      } @else if (!loading()) {
        <form class="premium-card rounded-[28px] p-8 max-w-3xl space-y-5" (ngSubmit)="submitSchool()">
          <h3 class="text-xl font-bold tracking-tight">Mon auto-école</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div><label class="field-label" for="name">Nom de l'établissement</label>
              <input id="name" class="field-input" name="name" required [(ngModel)]="school.name" /></div>
            <div><label class="field-label" for="phone">Téléphone</label>
              <input id="phone" class="field-input" name="phone" required [(ngModel)]="school.phoneNumber" /></div>
            <div><label class="field-label" for="email">Email</label>
              <input id="email" class="field-input" type="email" name="email" [(ngModel)]="school.email" /></div>
            <div><label class="field-label" for="whatsapp">WhatsApp</label>
              <input id="whatsapp" class="field-input" name="whatsapp" [(ngModel)]="school.whatsappNumber" /></div>
            <div><label class="field-label" for="country">Pays</label>
              <input id="country" class="field-input" name="country" [(ngModel)]="school.country" /></div>
            <div><label class="field-label" for="city">Ville</label>
              <input id="city" class="field-input" name="city" [(ngModel)]="school.city" /></div>
            <div class="md:col-span-2"><label class="field-label" for="address">Adresse</label>
              <input id="address" class="field-input" name="address" required [(ngModel)]="school.address" /></div>
            <div class="md:col-span-2"><label class="field-label" for="website">Site web</label>
              <input id="website" class="field-input" name="website" [(ngModel)]="school.websiteUrl" /></div>
            <div class="md:col-span-2"><label class="field-label" for="description">Présentation</label>
              <textarea id="description" class="field-input min-h-28" name="description" [(ngModel)]="school.description"></textarea></div>
          </div>
          <button type="submit" class="btn-primary" [disabled]="sending()">Envoyer la demande</button>
        </form>

        <p class="mt-8 text-sm text-black/50 dark:text-white/50">
          Vous êtes moniteur salarié d'une auto-école déjà présente ?
          <a routerLink="/auto-ecoles" class="text-[#0070f3] font-semibold">Rejoignez-la depuis le catalogue</a>.
        </p>
      }
    } @else {
      <div class="mb-8">
        <a routerLink="/auto-ecoles" class="btn-primary"><lucide-icon [img]="icons.Search" [size]="16" /> Trouver une auto-école</a>
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
  private readonly router = inject(Router);

  protected readonly loading = signal(true);
  protected readonly registry = signal<MySchoolRegistry | null>(null);
  protected readonly joinRequests = signal<MyJoinRequest[]>([]);
  protected readonly sending = signal(false);
  protected readonly refreshing = signal(false);
  protected readonly info = signal('');
  protected readonly error = signal('');

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
  }

  protected submitSchool(): void {
    this.sending.set(true);
    this.error.set('');
    this.api.requestSchool(this.school).subscribe({
      next: (res) => { this.sending.set(false); this.info.set(res.message); this.load(); },
      error: (err) => { this.sending.set(false); this.error.set(errorMessage(err)); },
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
          this.info.set('Votre accès n\'est pas encore ouvert : la demande est toujours en attente de validation.');
        }
      },
      error: (err) => { this.refreshing.set(false); this.error.set(errorMessage(err)); },
    });
  }
}
