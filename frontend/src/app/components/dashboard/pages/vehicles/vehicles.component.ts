import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Vehicle, VehicleRequest } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

/** Parc de véhicules de l'auto-école (moniteur). */
@Component({
  selector: 'app-vehicles',
  imports: [FormsModule, LucideAngularModule, PageHeaderComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Véhicules" subtitle="Seuls les véhicules disponibles peuvent être réservés pour une leçon.">
      <button class="btn-primary" (click)="startCreate()"><lucide-icon [img]="icons.Plus" [size]="16" /> Ajouter</button>
    </app-page-header>
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (formOpen()) {
      <form class="premium-card rounded-[24px] p-6 mb-8 grid grid-cols-1 md:grid-cols-4 gap-4 items-end" (ngSubmit)="save()">
        <div><label class="field-label" for="mat">Immatriculation</label>
          <input id="mat" class="field-input" name="mat" required [(ngModel)]="form.matriculation" /></div>
        <div><label class="field-label" for="model">Modèle</label>
          <input id="model" class="field-input" name="model" required [(ngModel)]="form.model" /></div>
        <div><label class="field-label" for="state">État</label>
          <select id="state" class="field-input" name="state" [(ngModel)]="form.state">
            <option value="DISPOSABLE">Disponible</option><option value="MAINTENANCE">En maintenance</option><option value="PANNE">En panne</option>
          </select></div>
        <div class="flex gap-2">
          <button type="submit" class="btn-primary">{{ editId ? 'Enregistrer' : 'Ajouter' }}</button>
          <button type="button" class="btn-ghost" (click)="formOpen.set(false)">Annuler</button>
        </div>
      </form>
    }

    <div class="premium-card rounded-[24px] p-4 overflow-x-auto">
      <table class="data-table">
        <thead><tr><th>Immatriculation</th><th>Modèle</th><th>État</th><th></th></tr></thead>
        <tbody>
          @for (v of vehicles(); track v.id) {
            <tr>
              <td class="font-semibold">{{ v.matriculation }}</td><td>{{ v.model }}</td><td><app-status-badge [value]="v.state" /></td>
              <td class="whitespace-nowrap text-right">
                <button class="btn-small hover:bg-black/5 dark:hover:bg-white/5" (click)="startEdit(v)" aria-label="Modifier"><lucide-icon [img]="icons.Pencil" [size]="15" /></button>
                <button class="btn-small text-red-600 hover:bg-red-500/10" (click)="remove(v)" aria-label="Supprimer"><lucide-icon [img]="icons.Trash2" [size]="15" /></button>
              </td>
            </tr>
          } @empty {
            <tr><td colspan="4" class="text-center text-black/50 dark:text-white/50">Aucun véhicule enregistré.</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class VehiclesComponent {
  protected readonly icons = ICONS;
  private readonly api = inject(SchoolApiService);
  protected readonly vehicles = signal<Vehicle[]>([]);
  protected readonly formOpen = signal(false);
  protected readonly error = signal('');
  protected editId: string | null = null;
  protected form: VehicleRequest = { matriculation: '', model: '', state: 'DISPOSABLE' };

  constructor() {
    this.load();
  }

  private load(): void {
    this.api.vehicles(0, 200).subscribe({ next: (p) => this.vehicles.set(p.content), error: (err) => this.error.set(errorMessage(err)) });
  }

  protected startCreate(): void {
    this.editId = null;
    this.form = { matriculation: '', model: '', state: 'DISPOSABLE' };
    this.formOpen.set(true);
  }

  protected startEdit(v: Vehicle): void {
    this.editId = v.id;
    this.form = { matriculation: v.matriculation, model: v.model, state: v.state };
    this.formOpen.set(true);
  }

  protected save(): void {
    const call = this.editId ? this.api.updateVehicle(this.editId, this.form) : this.api.createVehicle(this.form);
    call.subscribe({
      next: () => { this.formOpen.set(false); this.error.set(''); this.load(); },
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected remove(v: Vehicle): void {
    if (!confirm(`Supprimer le véhicule ${v.matriculation} ?`)) return;
    this.api.deleteVehicle(v.id).subscribe({ next: () => this.load(), error: (err) => this.error.set(errorMessage(err)) });
  }
}
