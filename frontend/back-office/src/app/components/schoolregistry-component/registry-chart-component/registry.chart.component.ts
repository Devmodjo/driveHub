import { Component, Input, OnChanges, ViewChild } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, Chart, registerables } from 'chart.js';
import { RegistryStats } from '../../../interfaces/RegistryStats';

Chart.register(...registerables);

/**
 * Affiche deux graphiques cote a cote :
 * - Un bar chart des inscriptions mensuelles (12 derniers mois)
 * - Un doughnut chart de la repartition par statut
 *
 * Les donnees mensuelles sont simulees car l'API ne fournit pas
 * encore de serie temporelle. Le doughnut utilise les stats reelles.
 */
@Component({
  selector: 'registry-chart',
  imports: [BaseChartDirective],
  templateUrl: './registry.chart.component.html',
  styleUrl: './registry.chart.component.css',
})
export class RegistryChartComponent implements OnChanges {
  @Input() stats: RegistryStats | null = null;

  @ViewChild('barChart') barChart?: BaseChartDirective;
  @ViewChild('doughnutChart') doughnutChart?: BaseChartDirective;

  barChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: {
      duration: 1500,
      easing: 'easeOutQuart'
    },
    plugins: {
      legend: { display: false },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { font: { size: 11, family: 'Lato' }, color: '#6b7280' },
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(0,0,0,0.04)' },
        ticks: { font: { size: 11, family: 'Lato' }, color: '#6b7280' },
      },
    },
  };

  doughnutChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '65%',
    animation: {
      animateScale: true,
      animateRotate: true,
      duration: 1500,
      easing: 'easeOutQuart'
    },
    plugins: {
      legend: { display: false },
    },
  };

  legendItems: { label: string; color: string; value: number }[] = [];

  ngOnChanges(): void {
    if (!this.stats) return;
    this.buildBarChart();
    this.buildDoughnutChart();
    
    // Force l'actualisation si le canevas est deja initialise
    if (this.barChart) {
      this.barChart.update();
    }
    if (this.doughnutChart) {
      this.doughnutChart.update();
    }
  }

  /**
   * Genere les donnees du bar chart.
   * Comme l'API ne retourne pas de serie mensuelle, on utilise
   * le total et la valeur du mois courant pour simuler la tendance.
   */
  private buildBarChart(): void {
    const months = ['J', 'F', 'M', 'A', 'M', 'J', 'J', 'A', 'S', 'O', 'N', 'D'];
    const currentMonth = new Date().getMonth();
    const base = Math.max(1, Math.floor(this.stats!.totalRegistries / 14));

    const data = months.map((_, i) => {
      if (i === currentMonth) return this.stats!.newThisMonth;
      if (i < currentMonth) return base + Math.floor(Math.random() * (base * 0.5));
      return 0;
    });

    this.barChartData = {
      labels: months,
      datasets: [{
        data,
        backgroundColor: 'rgba(0, 86, 179, 0.7)',
        borderRadius: 4,
        barPercentage: 0.6,
      }],
    };
  }

  /** Construit le doughnut a partir des stats reelles de l'API. */
  private buildDoughnutChart(): void {
    const s = this.stats!;
    this.legendItems = [
      { label: 'Actives', color: '#10b981', value: s.activeRegistries },
      { label: 'En attente', color: '#f59e0b', value: s.pendingRegistries },
      { label: 'Rejetees', color: '#ef4444', value: s.rejectedRegistries },
      { label: 'Suspendues', color: '#6b7280', value: s.suspendedRegistries },
    ];

    this.doughnutChartData = {
      labels: this.legendItems.map(i => i.label),
      datasets: [{
        data: this.legendItems.map(i => i.value),
        backgroundColor: this.legendItems.map(i => i.color),
        borderWidth: 0,
        hoverOffset: 4,
      }],
    };
  }
}
