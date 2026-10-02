import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { LicenseCategory, Student } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { LICENSE_CATEGORIES, formatDate } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';

/** Élèves de l'auto-école (moniteur) : catégorie de permis, pièces d'identité, retrait. */
@Component({
  selector: 'app-students',
  imports: [FormsModule, LucideAngularModule, PageHeaderComponent],
  template: `
    <app-page-header title="Élèves" subtitle="Les élèves arrivent par les demandes d'adhésion que vous approuvez." />
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (editing(); as s) {
      <form class="premium-card rounded-[24px] p-6 mb-8 grid grid-cols-1 md:grid-cols-4 gap-4 items-end" (ngSubmit)="save(s)">
        <p class="md:col-span-4 font-bold">{{ s.firstname }} {{ s.lastname }}</p>
        <div><label class="field-label" for="cat">Catégorie</label>
          <select id="cat" class="field-input" name="cat" [(ngModel)]="form.licenseCategory">
            @for (c of categories; track c) { <option [value]="c">{{ c }}</option> }
          </select></div>
        <div><label class="field-label" for="recto">CNI recto (lien)</label>
          <input id="recto" class="field-input" name="recto" [(ngModel)]="form.cniRectoUrl" /></div>
        <div><label class="field-label" for="verso">CNI verso (lien)</label>
          <input id="verso" class="field-input" name="verso" [(ngModel)]="form.cniVersoUrl" /></div>
        <div class="flex gap-2">
          <button type="submit" class="btn-primary">Enregistrer</button>
          <button type="button" class="btn-ghost" (click)="editing.set(null)">Annuler</button>
        </div>
      </form>
    }

    <div class="premium-card rounded-[24px] p-4 overflow-x-auto">
      <table class="data-table">
        <thead><tr><th>Nom</th><th>Email</th><th>Téléphone</th><th>Ville</th><th>Permis</th><th>Inscrit le</th><th></th></tr></thead>
        <tbody>
          @for (s of students(); track s.id) {
            <tr>
              <td class="font-semibold">{{ s.firstname }} {{ s.lastname }}</td><td>{{ s.email }}</td><td>{{ s.phoneNumber }}</td>
              <td>{{ s.residenceCity }}</td><td>{{ s.licenseCategory ?? '-' }}</td><td>{{ formatDate(s.createdOn) }}</td>
              <td class="whitespace-nowrap text-right">
                <button class="btn-small hover:bg-black/5 dark:hover:bg-white/5" (click)="edit(s)" aria-label="Modifier"><lucide-icon [img]="icons.Pencil" [size]="15" /></button>
                <button class="btn-small text-red-600 hover:bg-red-500/10" (click)="remove(s)" aria-label="Retirer"><lucide-icon [img]="icons.Trash2" [size]="15" /></button>
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="text-center text-black/50 dark:text-white/50">Aucun élève pour le moment.</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class StudentsComponent {
  protected readonly icons = ICONS;
  protected readonly categories = LICENSE_CATEGORIES;
  protected readonly formatDate = formatDate;
  private readonly api = inject(SchoolApiService);

  protected readonly students = signal<Student[]>([]);
  protected readonly editing = signal<Student | null>(null);
  protected readonly error = signal('');
  protected form: { licenseCategory: LicenseCategory; cniRectoUrl: string; cniVersoUrl: string } =
    { licenseCategory: 'B', cniRectoUrl: '', cniVersoUrl: '' };

  constructor() {
    this.load();
  }

  private load(): void {
    this.api.students(0, 200).subscribe({
      next: (page) => this.students.set(page.content),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected edit(s: Student): void {
    this.form = { licenseCategory: s.licenseCategory ?? 'B', cniRectoUrl: s.cniRectoUrl ?? '', cniVersoUrl: s.cniVersoUrl ?? '' };
    this.editing.set(s);
  }

  protected save(s: Student): void {
    this.api.updateStudent(s.id, {
      licenseCategory: this.form.licenseCategory,
      cniRectoUrl: this.form.cniRectoUrl || null,
      cniVersoUrl: this.form.cniVersoUrl || null,
    }).subscribe({
      next: () => { this.editing.set(null); this.load(); },
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected remove(s: Student): void {
    if (!confirm(`Retirer ${s.firstname} ${s.lastname} de l'auto-école ?`)) return;
    this.api.deleteStudent(s.id).subscribe({ next: () => this.load(), error: (err) => this.error.set(errorMessage(err)) });
  }
}
