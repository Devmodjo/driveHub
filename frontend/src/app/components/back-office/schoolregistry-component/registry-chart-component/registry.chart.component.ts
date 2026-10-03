import { Component, effect, inject, Input, OnChanges, ViewChild } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, Chart, registerables } from 'chart.js';
import { RegistryStats } from '../../../../interfaces/RegistryStats';
import { ThemeService } from '../../../../services/theme-service/theme.service';

Chart.register(...registerables);

/**
 * Deux graphiques (ng2-charts / Chart.js) :
 * - barres : inscriptions par mois de l'année en cours ;
 * - anneau : répartition des auto-écoles par statut.
 *
 * L'API ne fournit pas encore de série mensuelle : seul le mois courant est réel (newThisMonth),
 * les mois précédents sont estimés à partir du total (le gabarit le signale à l'utilisateur).
 * L'anneau utilise les statistiques réelles.
 * Les couleurs des axes suivent le thème clair / sombre (ThemeService).
 */
@Component({
  selector: 'registry-chart',
  imports: [BaseChartDirective],
  templateUrl: './registry.chart.component.html',
})
export class RegistryChartComponent implements OnChanges {
  @Input() stats: RegistryStats | null = null;

  // read: BaseChartDirective : sans cette option, la référence #barChart désignerait le <canvas>
  // lui-même (ElementRef) et non la directive ng2-charts qui possède update() / render().
  @ViewChild('barChart', { read: BaseChartDirective }) barChart?: BaseChartDirective;
  @ViewChild('doughnutChart', { read: BaseChartDirective }) doughnutChart?: BaseChartDirective;

  private readonly theme = inject(ThemeService);

  barChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  barChartOptions: ChartConfiguration<'bar'>['options'] = this.buildBarOptions(false);

  doughnutChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '68%',
    animation: { animateScale: true, animateRotate: true, duration: 900, easing: 'easeOutQuart' },
    plugins: { legend: { display: false } },
  };

  legendItems: { label: string; color: string; value: number }[] = [];

  constructor() {
    // Quand le thème change, on recrée les options pour adapter la couleur des axes et de la grille
    effect(() => {
      this.barChartOptions = this.buildBarOptions(this.theme.isDark());
      this.barChart?.render();
    });
  }

  ngOnChanges(): void {
    if (!this.stats) return;
    this.buildBarChart();
    this.buildDoughnutChart();

    // Force l'actualisation si le canevas est déjà initialisé
    this.barChart?.update();
    this.doughnutChart?.update();
  }

  /** Options du graphique en barres, selon le thème (texte gris clair sur fond sombre). */
  private buildBarOptions(dark: boolean): ChartConfiguration<'bar'>['options'] {
    const tickColor = dark ? 'rgba(255,255,255,0.55)' : 'rgba(0,0,0,0.5)';
    const gridColor = dark ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.05)';
    return {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 900, easing: 'easeOutQuart' },
      plugins: { legend: { display: false } },
      scales: {
        x: { grid: { display: false }, ticks: { font: { size: 11, family: 'Lato' }, color: tickColor } },
        y: {
          beginAtZero: true,
          grid: { color: gridColor },
          ticks: { font: { size: 11, family: 'Lato' }, color: tickColor, precision: 0 },
        },
      },
    };
  }

  /**
   * Données du graphique en barres. Le mois courant vient de l'API (newThisMonth) ;
   * les mois passés sont estimés (moyenne du total, avec une légère variation stable
   * pour que la courbe ne change pas à chaque rechargement) ; les mois à venir valent 0.
   */
  private buildBarChart(): void {
    const months = ['J', 'F', 'M', 'A', 'M', 'J', 'J', 'A', 'S', 'O', 'N', 'D'];
    const currentMonth = new Date().getMonth();
    const base = Math.max(1, Math.floor(this.stats!.totalRegistries / 14));

    const data = months.map((_, i) => {
      if (i === currentMonth) return this.stats!.newThisMonth;
      if (i < currentMonth) return base + ((i * 7) % 3) * Math.max(1, Math.floor(base / 4));
      return 0;
    });

    this.barChartData = {
      labels: months,
      datasets: [{
        data,
        // Mois courant en bleu plein, mois estimés en bleu atténué
        backgroundColor: months.map((_, i) => (i === currentMonth ? '#0070f3' : 'rgba(0, 112, 243, 0.35)')),
        borderRadius: 6,
        barPercentage: 0.65,
      }],
    };
  }

  /** Construit l'anneau à partir des statistiques réelles de l'API. */
  private buildDoughnutChart(): void {
    const s = this.stats!;
    this.legendItems = [
      { label: 'Actives', color: '#10b981', value: s.activeRegistries },
      { label: 'En attente', color: '#f59e0b', value: s.pendingRegistries },
      { label: 'Rejetées', color: '#ef4444', value: s.rejectedRegistries },
      { label: 'Suspendues', color: '#6b7280', value: s.suspendedRegistries },
    ];

    this.doughnutChartData = {
      labels: this.legendItems.map((i) => i.label),
      datasets: [{
        data: this.legendItems.map((i) => i.value),
        backgroundColor: this.legendItems.map((i) => i.color),
        borderWidth: 0,
        hoverOffset: 4,
      }],
    };
  }
}
