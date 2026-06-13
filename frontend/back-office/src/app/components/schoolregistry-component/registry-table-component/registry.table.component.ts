import { Component, Input, Output, EventEmitter, computed, signal } from '@angular/core';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SchoolRegistryDetail } from '../../../interfaces/SchoolRegistryDetail';

/**
 * Tableau paginé des auto-ecoles avec onglets de filtrage par statut,
 * recherche par nom, et actions (voir, approuver, rejeter, supprimer).
 *
 * Ce composant ne gere pas la logique metier : il recoit les donnees
 * via @Input et emet les evenements utilisateur via @Output
 * pour que le composant parent orchestre les appels API.
 */
@Component({
  selector: 'registry-table',
  imports: [DatePipe, UpperCasePipe, FormsModule],
  templateUrl: './registry.table.component.html',
  styleUrl: './registry.table.component.css',
})
export class RegistryTableComponent {

  @Input() schools: SchoolRegistryDetail[] = [];
  @Input() isLoading: boolean = false;
  @Input() currentPage: number = 0;
  @Input() totalPages: number = 0;
  @Input() totalElements: number = 0;
  @Input() pageSize: number = 10;
  @Input() activeFilter: string | undefined = undefined;
  @Input() pendingCount: number = 0;

  @Output() pageChange = new EventEmitter<number>();
  @Output() filterChange = new EventEmitter<string | undefined>();
  @Output() approve = new EventEmitter<string>();
  @Output() reject = new EventEmitter<string>();
  @Output() viewDetail = new EventEmitter<string>();
  @Output() deleteSchool = new EventEmitter<string>();

  searchQuery = signal('');

  /** Filtre local les ecoles affichees en fonction de la recherche par nom. */
  filteredSchools = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.schools;
    return this.schools.filter(s => s.schoolName.toLowerCase().includes(query));
  });

  /** Calcule le numero du premier element affiche sur la page courante. */
  get startIndex(): number {
    return this.currentPage * this.pageSize + 1;
  }

  /** Calcule le numero du dernier element affiche sur la page courante. */
  get endIndex(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  /** Genere la liste des numeros de page pour la navigation. */
  get pageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible);
    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }
    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  }

  applyFilter(status: string | undefined): void {
    this.filterChange.emit(status);
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.pageChange.emit(page);
    }
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
  }

  /** Retourne un libelle lisible pour le statut d'une auto-ecole. */
  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Active',
      PENDING: 'En attente',
      REJECTED: 'Rejetee',
      SUSPENDED: 'Suspendue',
      INACTIVE: 'Inactive',
    };
    return labels[status] ?? status;
  }

  /** Retourne la classe CSS associee au statut pour le badge colore. */
  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      ACTIVE: 'badge badge-green',
      PENDING: 'badge badge-amber',
      REJECTED: 'badge badge-red',
      SUSPENDED: 'badge badge-gray',
      INACTIVE: 'badge badge-gray',
    };
    return classes[status] ?? 'badge';
  }
}
