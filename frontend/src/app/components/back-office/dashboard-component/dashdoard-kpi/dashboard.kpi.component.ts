import { Component, Input } from '@angular/core';

@Component({
  selector: 'dashboard-kpi',
  imports: [],
  template: `
    <div class="kpi-card">
      <div class="kpi-header">
        <div class="flex flex-col gap-1">
          <h3 class="kpi-title">{{ KpiTitle }}</h3>
          <span class="kpi-result" [style.color]="KpiColor">{{ KpiResult ?? '0' }}</span>
        </div>
        <div class="kpi-icon-wrapper" [style.color]="KpiColor" [style.backgroundColor]="KpiBgColor">
          <ng-content></ng-content>
        </div>
      </div>
    </div>
  `,
  styleUrl: './dashboard.kpi.component.css',
})
export class DashboardKpiComponent {
  @Input() KpiTitle?: string;
  @Input() KpiResult?: number | string;
  @Input() KpiColor?: string = 'var(--text-primary)';
  @Input() KpiBgColor?: string = 'transparent';
}

