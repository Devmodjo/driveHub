import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Course, CourseRequest } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { formatDate } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';

/** Cours théoriques : le moniteur les publie, les élèves les consultent. */
@Component({
  selector: 'app-courses',
  imports: [FormsModule, LucideAngularModule, PageHeaderComponent],
  template: `
    <app-page-header title="Cours" [subtitle]="isMonitor ? 'Supports de cours partagés avec vos élèves.' : 'Les supports publiés par votre auto-école.'">
      @if (isMonitor) {
        <button class="btn-primary" (click)="startCreate()"><lucide-icon [img]="icons.Plus" [size]="16" /> Nouveau cours</button>
      }
    </app-page-header>
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (formOpen()) {
      <form class="premium-card rounded-[24px] p-6 mb-8 space-y-4" (ngSubmit)="save()">
        <div><label class="field-label" for="title">Titre</label>
          <input id="title" class="field-input" name="title" required [(ngModel)]="form.title" /></div>
        <div><label class="field-label" for="content">Contenu</label>
          <textarea id="content" class="field-input min-h-40" name="content" required [(ngModel)]="form.content"></textarea></div>
        <div class="flex gap-2">
          <button type="submit" class="btn-primary">Enregistrer</button>
          <button type="button" class="btn-ghost" (click)="formOpen.set(false)">Annuler</button>
        </div>
      </form>
    }

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      @for (c of courses(); track c.id) {
        <article class="premium-card rounded-[24px] p-6">
          <div class="flex items-start justify-between gap-4 mb-3">
            <h3 class="text-lg font-bold tracking-tight">{{ c.title }}</h3>
            @if (isMonitor) {
              <div class="flex shrink-0">
                <button class="btn-small hover:bg-black/5 dark:hover:bg-white/5" (click)="startEdit(c)" aria-label="Modifier"><lucide-icon [img]="icons.Pencil" [size]="15" /></button>
                <button class="btn-small text-red-600 hover:bg-red-500/10" (click)="remove(c)" aria-label="Supprimer"><lucide-icon [img]="icons.Trash2" [size]="15" /></button>
              </div>
            }
          </div>
          <p class="text-sm text-black/60 dark:text-white/60 font-light whitespace-pre-line">{{ c.content }}</p>
          <p class="text-xs text-black/40 dark:text-white/40 mt-4">Publié le {{ formatDate(c.createdAt) }}</p>
        </article>
      } @empty {
        <p class="text-black/50 dark:text-white/50">Aucun cours publié.</p>
      }
    </div>
  `,
})
export class CoursesComponent {
  protected readonly icons = ICONS;
  protected readonly formatDate = formatDate;
  private readonly api = inject(SchoolApiService);
  protected readonly isMonitor = inject(SessionService).isMonitor();
  protected readonly courses = signal<Course[]>([]);
  protected readonly formOpen = signal(false);
  protected readonly error = signal('');
  protected editId: string | null = null;
  protected form: CourseRequest = { title: '', content: '' };

  constructor() {
    this.load();
  }

  private load(): void {
    this.api.courses(0, 200).subscribe({ next: (p) => this.courses.set(p.content), error: (err) => this.error.set(errorMessage(err)) });
  }

  protected startCreate(): void {
    this.editId = null;
    this.form = { title: '', content: '' };
    this.formOpen.set(true);
  }

  protected startEdit(c: Course): void {
    this.editId = c.id;
    this.form = { title: c.title, content: c.content };
    this.formOpen.set(true);
  }

  protected save(): void {
    const call = this.editId ? this.api.updateCourse(this.editId, this.form) : this.api.createCourse(this.form);
    call.subscribe({
      next: () => { this.formOpen.set(false); this.error.set(''); this.load(); },
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected remove(c: Course): void {
    if (!confirm(`Supprimer le cours « ${c.title} » ?`)) return;
    this.api.deleteCourse(c.id).subscribe({ next: () => this.load(), error: (err) => this.error.set(errorMessage(err)) });
  }
}
