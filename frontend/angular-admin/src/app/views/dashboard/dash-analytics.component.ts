// angular import
import { Component, inject, ViewChild } from '@angular/core';

// project import
import { SharedModule } from 'src/app/theme/shared/shared.module';

// 3rd party import

import { ApexOptions, ChartComponent, NgApexchartsModule } from 'ng-apexcharts';
import { Router } from '@angular/router';
@Component({
  selector: 'app-dash-analytics',
  imports: [SharedModule, NgApexchartsModule],
  templateUrl: './dash-analytics.component.html',
  styleUrls: ['./dash-analytics.component.scss']
})
export class DashAnalyticsComponent{
  @ViewChild('chart') chart!: ChartComponent;
  @ViewChild('customerChart') customerChart!: ChartComponent;

  public router = inject(Router);

  // Données pour les cartes
  cards = [
    {
      title: 'Nombre de D\'utilisateurs',
      number: '10',
      text: 'Ce Mois',
      no: '3,564',
      icon: 'icon-user',
      background: 'bg-c-green'
    },
    {
      title: 'Commandes Reçues',
      number: '486',
      text: 'Ce Mois',
      no: '351',
      icon: 'icon-shopping-cart',
      background: 'bg-c-blue'
    },
    {
      title: 'Nombre de consultations',
      number: '1,250',
      text: 'Ce Mois',
      no: '824',
      icon: 'icon-shopping-cart',
      background: 'bg-c-yellow'
    }
  ];

  // Options pour le graphique des visiteurs uniques
  chartOptions: Partial<ApexOptions> = {
    chart: { height: 205, type: 'line', toolbar: { show: false } },
    dataLabels: { enabled: false },
    stroke: { width: 2, curve: 'smooth' },
    series: [
      { name: 'Régimes', data: [20, 50, 30, 60, 30, 50, 15] },
      { name: 'Consultations', data: [60, 30, 65, 45, 67, 35] }
    ],
    xaxis: {
      type: 'category',
      categories: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin'],
      axisBorder: { show: false }
    },
    yaxis: { show: true, min: 10, max: 70 },
    colors: ['#73b4ff', '#59e0c5'],
    fill: {
      type: 'gradient',
      gradient: {
        shade: 'light',
        gradientToColors: ['#4099ff', '#2ed8b6'],
        shadeIntensity: 0.5,
        type: 'horizontal',
        opacityFrom: 1,
        opacityTo: 1,
        stops: [0, 100]
      }
    },
    grid: { borderColor: '#cccccc3b' }
  };

  // Options pour le graphique des clients (nouveaux vs. retournés)
  chartOptions_1: Partial<ApexOptions> = {
    chart: { height: 150, type: 'donut' },
    dataLabels: { enabled: false },
    plotOptions: { pie: { donut: { size: '75%' } } },
    labels: ['Nouveaux', 'Récurrents'],
    series: [674, 182],
    legend: { show: false },
    tooltip: { theme: 'dark' },
    colors: ['#4680ff', '#2ed8b6'],
    fill: { opacity: [1, 1] },
    stroke: { width: 0 },
    grid: { padding: { top: 20, right: 0, bottom: 0, left: 0 } }
  };

  // Options pour le graphique des clients (version sombre)
  chartOptions_2: Partial<ApexOptions> = {
    chart: { height: 150, type: 'donut' },
    dataLabels: { enabled: false },
    plotOptions: { pie: { donut: { size: '75%' } } },
    labels: ['Nouveaux', 'Récurrents'],
    series: [674, 182],
    legend: { show: false },
    tooltip: { theme: 'dark' },
    colors: ['#fff', '#2ed8b6'],
    fill: { opacity: [1, 1] },
    stroke: { width: 0 },
    grid: { padding: { top: 20, right: 0, bottom: 0, left: 0 } }
  };

  // Options pour le graphique de mémoire
  chartOptions_3: Partial<ApexOptions> = {
    chart: { type: 'area', height: 145, sparkline: { enabled: true } },
    dataLabels: { enabled: false },
    colors: ['#ff5370'],
    fill: {
      type: 'gradient',
      gradient: {
        shade: 'dark',
        gradientToColors: ['#ff869a'],
        shadeIntensity: 1,
        type: 'horizontal',
        opacityFrom: 1,
        opacityTo: 0.8,
        stops: [0, 100, 100, 100]
      }
    },
    stroke: { curve: 'smooth', width: 2 },
    series: [{ data: [45, 35, 60, 50, 85, 70] }],
    yaxis: { min: 5, max: 90 },
    tooltip: { fixed: { enabled: false }, x: { show: false }, marker: { show: false } }
  };

  newsLetterNumber = 0;
  
}
