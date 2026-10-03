import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { SchoolRegistryDetail } from '../../../../interfaces/SchoolRegistryDetail';
import { ICONS } from '../../../../shared/icons';
import { schoolStatusClass, schoolStatusLabel } from '../../shared/bo-status';

/**
 * Liste paginée des auto-écoles : filtres par statut, recherche sur la page courante
 * et actions (voir, approuver, rejeter, suspendre, réactiver, supprimer).
 *
 * - Mobile : une carte par auto-école, boutons larges.
 * - À partir de md: : tableau ; les colonnes secondaires (ville, responsable, date)
 *   n'apparaissent qu'à partir de lg: / xl: pour garder les actions visibles.
 *
 * Ce composant ne contient pas de logique métier : il reçoit les données via @Input
 * et émet les actions via @Output ; la page parente (SchoolComponent) appelle l'API.
 */
@Component({
  selector: 'registry-table',
  imports: [DatePipe, UpperCasePipe, FormsModule, LucideDynamicIcon],
  templateUrl: './registry.table.component.html',
})
export class RegistryTableComponent {
  protected readonly icons = ICONS;
  protected readonly statusLabel = schoolStatusLabel;
  protected readonly statusClass = schoolStatusClass;

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
  @Output() suspend = new EventEmitter<string>();
  @Output() reactivate = new EventEmitter<string>();

  /** Onglets de filtre (value undefined = toutes les auto-écoles). */
  protected readonly tabs: { label: string; value: string | undefined }[] = [
    { label: 'Toutes', value: undefined },
    { label: 'En attente', value: 'PENDING' },
    { label: 'Actives', value: 'ACTIVE' },
    { label: 'Approuvées', value: 'APPROVED' },
    { label: 'Suspendues', value: 'SUSPENDED' },
    { label: 'Rejetées', value: 'REJECTED' },
  ];

  searchQuery = signal('');

  /**
   * Auto-écoles affichées : celles de la page courante, filtrées par la recherche (nom ou ville).
   * Simple méthode (et non computed) : « schools » est un @Input classique, qu'un computed
   * ne verrait pas changer.
   */
  filteredSchools(): SchoolRegistryDetail[] {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.schools;
    return this.schools.filter((s) =>
      (s.schoolName ?? '').toLowerCase().includes(query) || (s.city ?? '').toLowerCase().includes(query));
  }

  /** Numéro du premier élément affiché sur la page courante. */
  get startIndex(): number {
    return this.currentPage * this.pageSize + 1;
  }

  /** Numéro du dernier élément affiché sur la page courante. */
  get endIndex(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  /** Numéros de page affichés (5 au maximum, centrés sur la page courante). */
  get pageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    const end = Math.min(this.totalPages, start + maxVisible);
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
}
