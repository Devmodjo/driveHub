import { Component, computed, input } from '@angular/core';
import { badgeClass, label } from './labels';

/** Badge coloré pour un statut (PENDING, APPROVED, VALIDATE...). */
@Component({
  selector: 'app-status-badge',
  template: `<span class="badge" [class]="css()">{{ text() }}</span>`,
})
export class StatusBadgeComponent {
  readonly value = input<string | null | undefined>(null);
  protected readonly text = computed(() => label(this.value()));
  protected readonly css = computed(() => badgeClass(this.value()));
}
