import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Exam, ExamInscription, ExamRequest, Student } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { LICENSE_CATEGORIES, formatDateTime, toLocalDateTime } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

/** Sessions d'examen : le moniteur crée les sessions et y inscrit ses élèves ; l'élève suit ses inscriptions. */
@Component({
  selector: 'app-exams',
  imports: [FormsModule, LucideAngularModule, PageHeaderComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Examens" [subtitle]="isMonitor ? 'Sessions d\\'examen et inscriptions de vos élèves.' : 'Vos inscriptions aux examens du permis.'">
      @if (isMonitor) {
        <button class="btn-primary" (click)="formOpen.set(!formOpen())"><lucide-icon [img]="icons.Plus" [size]="16" /> Nouvelle session</button>
      }
    </app-page-header>
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (isMonitor) {
      @if (formOpen()) {
        <form class="premium-card rounded-[24px] p-6 mb-8 grid grid-cols-1 md:grid-cols-3 gap-4 items-end" (ngSubmit)="create()">
          <div><label class="field-label" for="date">Date et heure</label>
            <input id="date" class="field-input" type="datetime-local" name="date" required [(ngModel)]="form.dateExams" /></div>
          <div><label class="field-label" for="cat">Catégorie</label>
            <select id="cat" class="field-input" name="cat" [(ngModel)]="form.category">
              @for (c of categories; track c) { <option [value]="c">{{ c }}</option> }
            </select></div>
          <button type="submit" class="btn-primary">Créer la session</button>
        </form>
      }

      <div class="space-y-5">
        @for (exam of exams(); track exam.id) {
          <div class="premium-card rounded-[24px] p-6">
            <div class="flex flex-wrap items-center justify-between gap-4">
              <div>
                <p class="text-lg font-bold">Permis {{ exam.category }} - {{ formatDateTime(exam.dateExams) }}</p>
              </div>
              <div class="flex gap-2">
                <button class="btn-ghost" (click)="toggle(exam)">{{ openExam() === exam.id ? 'Masquer' : 'Inscriptions' }}</button>
                <button class="btn-small text-red-600 hover:bg-red-500/10" (click)="remove(exam)" aria-label="Supprimer"><lucide-icon [img]="icons.Trash2" [size]="15" /></button>
              </div>
            </div>

            @if (openExam() === exam.id) {
              <div class="mt-6 pt-6 border-t border-black/5 dark:border-white/5">
                <form class="flex flex-wrap gap-3 items-end mb-5" (ngSubmit)="register(exam)">
                  <div class="grow max-w-sm"><label class="field-label" for="student">Inscrire un élève</label>
                    <select id="student" class="field-input" name="student" [(ngModel)]="studentId">
                      <option value="">Choisir...</option>
                      @for (s of students(); track s.id) { <option [value]="s.id">{{ s.firstname }} {{ s.lastname }}</option> }
                    </select></div>
                  <button type="submit" class="btn-primary" [disabled]="!studentId">Inscrire</button>
                </form>
                <table class="data-table">
                  <thead><tr><th>Élève</th><th>Inscrit le</th><th>Statut</th><th></th></tr></thead>
                  <tbody>
                    @for (i of inscriptions(); track i.id) {
                      <tr>
                        <td>{{ i.studentFirstname }} {{ i.studentLastname }}</td><td>{{ i.registeredAt }}</td>
                        <td><app-status-badge [value]="i.inscriptionStatus" /></td>
                        <td class="text-right whitespace-nowrap space-x-2">
                          @if (i.inscriptionStatus !== 'INSCRIT') { <button class="btn-small bg-[#0070f3] text-white" (click)="setStatus(exam, i, 'INSCRIT')">Valider</button> }
                          @if (i.inscriptionStatus !== 'REFUSE') { <button class="btn-small text-red-600 hover:bg-red-500/10" (click)="setStatus(exam, i, 'REFUSE')">Refuser</button> }
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="4" class="text-center text-black/50 dark:text-white/50">Aucun élève inscrit.</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            }
          </div>
        } @empty {
          <p class="text-black/50 dark:text-white/50">Aucune session d'examen programmée.</p>
        }
      </div>
    } @else {
      <div class="premium-card rounded-[24px] p-4 overflow-x-auto">
        <table class="data-table">
          <thead><tr><th>Date</th><th>Catégorie</th><th>Inscrit le</th><th>Statut</th></tr></thead>
          <tbody>
            @for (i of inscriptions(); track i.id) {
              <tr><td>{{ formatDateTime(i.dateExams) }}</td><td>Permis {{ i.category }}</td><td>{{ i.registeredAt }}</td>
                <td><app-status-badge [value]="i.inscriptionStatus" /></td></tr>
            } @empty {
              <tr><td colspan="4" class="text-center text-black/50 dark:text-white/50">Votre auto-école ne vous a pas encore inscrit à un examen.</td></tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class ExamsComponent {
  protected readonly icons = ICONS;
  protected readonly categories = LICENSE_CATEGORIES;
  protected readonly formatDateTime = formatDateTime;
  private readonly api = inject(SchoolApiService);
  protected readonly isMonitor = inject(SessionService).isMonitor();

  protected readonly exams = signal<Exam[]>([]);
  protected readonly students = signal<Student[]>([]);
  protected readonly inscriptions = signal<ExamInscription[]>([]);
  protected readonly openExam = signal<string | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly error = signal('');
  protected form: ExamRequest = { dateExams: '', category: 'B' };
  protected studentId = '';

  constructor() {
    if (this.isMonitor) {
      this.loadExams();
      this.api.students(0, 500).subscribe({ next: (p) => this.students.set(p.content) });
    } else {
      this.api.myExamInscriptions().subscribe({ next: (list) => this.inscriptions.set(list), error: (err) => this.fail(err) });
    }
  }

  private loadExams(): void {
    this.api.exams(0, 200).subscribe({ next: (p) => this.exams.set(p.content), error: (err) => this.fail(err) });
  }

  private loadInscriptions(examId: string): void {
    this.api.examInscriptions(examId).subscribe({ next: (list) => this.inscriptions.set(list), error: (err) => this.fail(err) });
  }

  protected create(): void {
    this.api.createExam({ ...this.form, dateExams: toLocalDateTime(this.form.dateExams) }).subscribe({
      next: () => { this.formOpen.set(false); this.error.set(''); this.loadExams(); },
      error: (err) => this.fail(err),
    });
  }

  protected toggle(exam: Exam): void {
    if (this.openExam() === exam.id) {
      this.openExam.set(null);
      return;
    }
    this.openExam.set(exam.id);
    this.inscriptions.set([]);
    this.loadInscriptions(exam.id);
  }

  protected register(exam: Exam): void {
    this.api.registerToExam(exam.id, this.studentId).subscribe({
      next: () => { this.studentId = ''; this.error.set(''); this.loadInscriptions(exam.id); },
      error: (err) => this.fail(err),
    });
  }

  protected setStatus(exam: Exam, i: ExamInscription, status: 'INSCRIT' | 'REFUSE'): void {
    this.api.setInscriptionStatus(i.id, status).subscribe({ next: () => this.loadInscriptions(exam.id), error: (err) => this.fail(err) });
  }

  protected remove(exam: Exam): void {
    if (!confirm('Supprimer cette session d\'examen ?')) return;
    this.api.deleteExam(exam.id).subscribe({ next: () => this.loadExams(), error: (err) => this.fail(err) });
  }

  private fail(err: unknown): void {
    this.error.set(errorMessage(err));
  }
}
