import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ReportService } from '../../core/services/report.service';
import {
  CategoryStockReportResponse,
  DashboardSummaryResponse,
  LowStockReportResponse,
  MovementPeriodReportResponse,
  ProductValuationReportResponse,
  ReportGranularity
} from '../../core/models/report.model';
import { ChartSeries } from '../../core/models/chart.model';
import { KpiCardComponent } from './components/kpi-card/kpi-card.component';
import { DonutChartComponent } from './components/donut-chart/donut-chart.component';
import { LineChartComponent } from './components/line-chart/line-chart.component';
import { BarChartComponent } from './components/bar-chart/bar-chart.component';

interface PeriodOption {
  days: number;
  label: string;
}

const EMPTY_SUMMARY: DashboardSummaryResponse = {
  totalProducts: 0,
  activeProducts: 0,
  totalCategories: 0,
  totalUnits: 0,
  lowStockProducts: 0,
  outOfStockProducts: 0,
  inventoryValue: 0,
  inboundMovements: 0,
  outboundMovements: 0,
  inboundUnits: 0,
  outboundUnits: 0,
  netUnits: 0,
  periodStart: '',
  periodEnd: '',
  generatedAt: ''
};

/**
 * Panel principal de analítica: KPIs del inventario y evolución de los movimientos.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    MatChipsModule,
    KpiCardComponent,
    DonutChartComponent,
    LineChartComponent,
    BarChartComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private reportService = inject(ReportService);

  readonly periodOptions: PeriodOption[] = [
    { days: 7, label: '7 días' },
    { days: 30, label: '30 días' },
    { days: 90, label: '90 días' },
    { days: 365, label: '12 meses' }
  ];

  readonly selectedDays = signal(30);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly summary = signal<DashboardSummaryResponse>(EMPTY_SUMMARY);
  readonly categoryValuation = signal<CategoryStockReportResponse[]>([]);
  readonly timeseries = signal<MovementPeriodReportResponse[]>([]);
  readonly topProducts = signal<ProductValuationReportResponse[]>([]);
  readonly lowStock = signal<LowStockReportResponse[]>([]);

  readonly lowStockColumns: string[] = [
    'sku',
    'productName',
    'categoryName',
    'quantity',
    'minStock',
    'shortage',
    'coveragePercentage',
    'severity'
  ];

  /** Límite del período en curso, calculado sobre la fecha de hoy en UTC. */
  readonly period = computed(() => {
    const days = this.selectedDays();
    const to = new Date();
    const from = new Date(to.getTime());
    from.setUTCDate(from.getUTCDate() - (days - 1));

    return {
      from: toIsoDate(from),
      to: toIsoDate(to)
    };
  });

  /** La granularidad se ajusta al tamaño del período para no saturar los gráficos. */
  readonly granularity = computed<ReportGranularity>(() => {
    const days = this.selectedDays();
    if (days <= 30) {
      return 'DAY';
    }
    if (days <= 90) {
      return 'WEEK';
    }
    return 'MONTH';
  });

  readonly timeseriesLabels = computed(() =>
    this.timeseries().map((period) => formatPeriodLabel(period.period, this.granularity()))
  );

  readonly timeseriesSeries = computed<ChartSeries[]>(() => {
    const periods = this.timeseries();

    return [
      {
        name: 'Entradas',
        color: '#009688',
        points: periods.map((period) => period.inboundUnits)
      },
      {
        name: 'Salidas',
        color: '#f44336',
        points: periods.map((period) => period.outboundUnits)
      }
    ];
  });

  readonly categorySegments = computed(() =>
    this.categoryValuation().map((category) => ({
      label: category.categoryName,
      value: category.totalValue
    }))
  );

  readonly topProductBars = computed(() =>
    this.topProducts().slice(0, 8).map((product) => ({
      label: product.productName,
      value: product.totalValue
    }))
  );

  readonly inventoryValueLabel = computed(() => this.formatCurrency(this.summary().inventoryValue));

  /** Formatea un importe monetario en euros. */
  readonly formatCurrency = (value: number): string =>
    new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
      maximumFractionDigits: 0
    }).format(value ?? 0);

  ngOnInit(): void {
    this.loadDashboard();
  }

  onPeriodChange(days: number): void {
    if (this.selectedDays() === days) {
      return;
    }

    this.selectedDays.set(days);
    this.loadDashboard();
  }

  reload(): void {
    this.loadDashboard();
  }

  severityLabel(severity: string): string {
    switch (severity) {
      case 'CRITICAL':
        return 'Crítico';
      case 'WARNING':
        return 'Atención';
      default:
        return 'Saludable';
    }
  }

  private loadDashboard(): void {
    const period = this.period();
    const filter = { from: period.from, to: period.to, granularity: this.granularity() };

    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      summary: this.reportService.getDashboardSummary(filter),
      valuation: this.reportService.getValuationByCategory(),
      timeseries: this.reportService.getMovementsTimeseries(filter),
      topProducts: this.reportService.getTopProducts({ ...filter, metric: 'VALUE', limit: 8 }),
      lowStock: this.reportService.getLowStock()
    })
      .pipe(
        catchError(() => {
          this.errorMessage.set('No se pudieron cargar los datos del dashboard');
          return of(null);
        })
      )
      .subscribe((result) => {
        if (result) {
          this.summary.set(result.summary);
          this.categoryValuation.set(result.valuation ?? []);
          this.timeseries.set(result.timeseries ?? []);
          this.topProducts.set(result.topProducts ?? []);
          this.lowStock.set(result.lowStock ?? []);
        }
        this.loading.set(false);
      });
  }
}

/**
 * Convierte una fecha a formato ISO (YYYY-MM-DD) usando el calendario UTC.
 */
function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

/**
 * Genera una etiqueta legible para cada período de la serie temporal.
 */
function formatPeriodLabel(period: string, granularity: ReportGranularity): string {
  if (!period) {
    return '';
  }

  if (granularity === 'MONTH') {
    const [year, month] = period.split('-');
    return month ? `${monthName(Number(month))} ${year?.slice(2)}` : period;
  }

  if (granularity === 'WEEK') {
    const match = period.match(/(\d{4})-W(\d{1,2})/);
    return match ? `S${match[2]}` : period;
  }

  const match = period.match(/(\d{4})-(\d{2})-(\d{2})/);
  if (!match) {
    return period;
  }

  return `${match[3]} ${monthName(Number(match[2]))}`;
}

/**
 * Devuelve el nombre corto del mes en español.
 */
function monthName(month: number): string {
  const names = [
    'ene',
    'feb',
    'mar',
    'abr',
    'may',
    'jun',
    'jul',
    'ago',
    'sep',
    'oct',
    'nov',
    'dic'
  ];

  return names[month - 1] ?? '';
}
