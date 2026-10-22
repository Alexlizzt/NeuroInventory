import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { CategoryService } from '../../core/services/category.service';
import { ReportService } from '../../core/services/report.service';
import { CategoryResponse } from '../../core/models/category.model';
import {
  CategoryStockReportResponse,
  DashboardSummaryResponse,
  LowStockReportResponse,
  MovementDetailReportResponse,
  MovementPeriodReportResponse,
  ProductValuationReportResponse,
  ReportGranularity,
  ReportMetric,
  MovementTypeFilter
} from '../../core/models/report.model';
import { downloadCsv } from '../../core/utils/csv-export';
import { DonutChartComponent } from '../dashboard/components/donut-chart/donut-chart.component';
import { LineChartComponent } from '../dashboard/components/line-chart/line-chart.component';
import { BarChartComponent } from '../dashboard/components/bar-chart/bar-chart.component';

type MovementTypeSelection = MovementTypeFilter | 'ALL';

/**
 * Resultado de la carga de un reporte, discriminado por la pestaña activa.
 */
type ReportResult =
  | {
      kind: 'summary';
      summary: DashboardSummaryResponse;
      timeseries: MovementPeriodReportResponse[];
      valuation: CategoryStockReportResponse[];
    }
  | {
      kind: 'valuation';
      valuation: CategoryStockReportResponse[];
    }
  | {
      kind: 'movements';
      movements: MovementDetailReportResponse[];
      timeseries: MovementPeriodReportResponse[];
    }
  | {
      kind: 'top';
      topProducts: ProductValuationReportResponse[];
    }
  | {
      kind: 'lowStock';
      lowStock: LowStockReportResponse[];
    };

/**
 * Reportes avanzados de inventario: filtros, tablas y exportación a CSV.
 */
@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTabsModule,
    MatTooltipModule,
    DonutChartComponent,
    LineChartComponent,
    BarChartComponent
  ],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  private reportService = inject(ReportService);
  private categoryService = inject(CategoryService);
  private snackBar = inject(MatSnackBar);

  readonly tabIndex = signal(0);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly from = signal<string | null>(null);
  readonly to = signal<string | null>(null);
  readonly granularity = signal<ReportGranularity>('DAY');
  readonly movementType = signal<MovementTypeSelection>('ALL');
  readonly categoryId = signal<string | null>(null);
  readonly limit = signal(50);
  readonly metric = signal<ReportMetric>('VALUE');

  readonly categories = signal<CategoryResponse[]>([]);
  readonly summary = signal<DashboardSummaryResponse | null>(null);
  readonly valuation = signal<CategoryStockReportResponse[]>([]);
  readonly timeseries = signal<MovementPeriodReportResponse[]>([]);
  readonly movements = signal<MovementDetailReportResponse[]>([]);
  readonly topProducts = signal<ProductValuationReportResponse[]>([]);
  readonly lowStock = signal<LowStockReportResponse[]>([]);

  readonly valuationColumns: string[] = [
    'categoryName',
    'productCount',
    'totalUnits',
    'totalValue',
    'percentage',
    'lowStockCount'
  ];

  readonly movementColumns: string[] = [
    'createdAt',
    'sku',
    'productName',
    'categoryName',
    'type',
    'quantity',
    'totalValue',
    'reason'
  ];

  readonly topProductColumns: string[] = [
    'sku',
    'productName',
    'categoryName',
    'quantity',
    'unitPrice',
    'totalValue',
    'movementCount'
  ];

  readonly lowStockColumns: string[] = [
    'sku',
    'productName',
    'categoryName',
    'quantity',
    'minStock',
    'shortage',
    'coveragePercentage',
    'suggestedPurchaseValue',
    'severity'
  ];

  readonly granularities: { value: ReportGranularity; label: string }[] = [
    { value: 'DAY', label: 'Diario' },
    { value: 'WEEK', label: 'Semanal' },
    { value: 'MONTH', label: 'Mensual' }
  ];

  readonly movementTypes: { value: MovementTypeSelection; label: string }[] = [
    { value: 'ALL', label: 'Todos' },
    { value: 'IN', label: 'Entradas' },
    { value: 'OUT', label: 'Salidas' },
    { value: 'ADJUSTMENT', label: 'Ajustes' }
  ];

  readonly metrics: { value: ReportMetric; label: string }[] = [
    { value: 'VALUE', label: 'Valor en inventario' },
    { value: 'MOVEMENT', label: 'Número de movimientos' }
  ];

  readonly limitOptions = [25, 50, 100, 250];

  readonly timeseriesLabels = computed(() =>
    this.timeseries().map((period) => this.formatPeriodLabel(period.period))
  );

  readonly timeseriesSeries = computed(() => {
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
    this.valuation().map((category) => ({
      label: category.categoryName,
      value: category.totalValue
    }))
  );

  readonly topProductBars = computed(() =>
    this.topProducts().map((product) => ({
      label: product.productName,
      value: this.metric() === 'MOVEMENT' ? product.movementCount : product.totalValue
    }))
  );

  readonly formatCurrency = (value: number): string =>
    new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
      maximumFractionDigits: 0
    }).format(value ?? 0);

  readonly formatNumber = (value: number): string =>
    new Intl.NumberFormat('es-ES').format(value ?? 0);

  ngOnInit(): void {
    this.loadCategories();
    this.loadActiveTab();
  }

  onTabChange(index: number): void {
    this.tabIndex.set(index);
    this.loadActiveTab();
  }

  applyFilters(): void {
    this.loadActiveTab();
  }

  clearFilters(): void {
    this.from.set(null);
    this.to.set(null);
    this.granularity.set('DAY');
    this.movementType.set('ALL');
    this.categoryId.set(null);
    this.limit.set(50);
    this.metric.set('VALUE');
    this.loadActiveTab();
  }

  onDateChange(field: 'from' | 'to', event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    (field === 'from' ? this.from : this.to).set(value || null);
  }

  onGranularityChange(value: ReportGranularity): void {
    this.granularity.set(value);
  }

  onMovementTypeChange(value: MovementTypeSelection): void {
    this.movementType.set(value);
  }

  onCategoryChange(value: string | null): void {
    this.categoryId.set(value);
  }

  onMetricChange(value: ReportMetric): void {
    this.metric.set(value);
  }

  onLimitChange(value: number): void {
    this.limit.set(value);
  }

  movementTypeLabel(type: string): string {
    switch (type) {
      case 'IN':
        return 'Entrada';
      case 'OUT':
        return 'Salida';
      case 'ADJUSTMENT':
        return 'Ajuste';
      default:
        return type;
    }
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

  exportValuation(): void {
    downloadCsv(
      'valoracion-categorias',
      [
        { header: 'Categoría', value: (row) => row.categoryName },
        { header: 'Productos', value: (row) => row.productCount },
        { header: 'Unidades', value: (row) => row.totalUnits },
        { header: 'Valor', value: (row) => row.totalValue },
        { header: 'Participación (%)', value: (row) => row.percentage },
        { header: 'Stock bajo', value: (row) => row.lowStockCount }
      ],
      this.valuation()
    );
    this.notifyExport('Valoración por categoría');
  }

  exportMovements(): void {
    downloadCsv(
      'movimientos-detalle',
      [
        { header: 'Fecha', value: (row) => row.createdAt },
        { header: 'SKU', value: (row) => row.sku },
        { header: 'Producto', value: (row) => row.productName },
        { header: 'Categoría', value: (row) => row.categoryName },
        { header: 'Tipo', value: (row) => this.movementTypeLabel(row.type) },
        { header: 'Cantidad', value: (row) => row.quantity },
        { header: 'Precio unitario', value: (row) => row.unitPrice },
        { header: 'Valor total', value: (row) => row.totalValue },
        { header: 'Motivo', value: (row) => row.reason },
        { header: 'Usuario', value: (row) => row.userId }
      ],
      this.movements()
    );
    this.notifyExport('Detalle de movimientos');
  }

  exportTopProducts(): void {
    downloadCsv(
      'productos-destacados',
      [
        { header: 'SKU', value: (row) => row.sku },
        { header: 'Producto', value: (row) => row.productName },
        { header: 'Categoría', value: (row) => row.categoryName },
        { header: 'Existencias', value: (row) => row.quantity },
        { header: 'Precio unitario', value: (row) => row.unitPrice },
        { header: 'Valor', value: (row) => row.totalValue },
        { header: 'Movimientos', value: (row) => row.movementCount }
      ],
      this.topProducts()
    );
    this.notifyExport('Productos destacados');
  }

  exportLowStock(): void {
    downloadCsv(
      'stock-bajo',
      [
        { header: 'SKU', value: (row) => row.sku },
        { header: 'Producto', value: (row) => row.productName },
        { header: 'Categoría', value: (row) => row.categoryName },
        { header: 'Existencias', value: (row) => row.quantity },
        { header: 'Stock mínimo', value: (row) => row.minStock },
        { header: 'Faltante', value: (row) => row.shortage },
        { header: 'Cobertura (%)', value: (row) => row.coveragePercentage },
        { header: 'Costo de reposición', value: (row) => row.suggestedPurchaseValue },
        { header: 'Severidad', value: (row) => this.severityLabel(row.severity) }
      ],
      this.lowStock()
    );
    this.notifyExport('Reporte de stock bajo');
  }

  /**
   * Informa al usuario que el reporte se ha descargado en formato CSV.
   */
  private notifyExport(name: string): void {
    this.snackBar.open(`${name} exportado en CSV`, 'Cerrar', { duration: 3000 });
  }

  private loadCategories(): void {
    this.categoryService
      .getCategories({ page: 0, size: 100, sortBy: 'name', direction: 'ASC' })
      .pipe(catchError(() => of(null)))
      .subscribe((result) => {
        this.categories.set(result?.content ?? []);
      });
  }

  private loadActiveTab(): void {
    const period = { from: this.from(), to: this.to() };

    this.loading.set(true);
    this.errorMessage.set(null);

    const request$ = this.buildRequest$(period);

    request$
      .pipe(
        catchError(() => {
          this.errorMessage.set('No se pudo generar el reporte solicitado');
          return of(null as ReportResult | null);
        })
      )
      .subscribe((result) => {
        if (result) {
          this.applyResult(result);
        }
        this.loading.set(false);
      });
  }

  private buildRequest$(period: { from: string | null; to: string | null }): Observable<ReportResult> {
    const movementType = this.movementType();

    switch (this.tabIndex()) {
      case 0:
        return forkJoin({
          summary: this.reportService.getDashboardSummary(period),
          timeseries: this.reportService.getMovementsTimeseries({
            ...period,
            granularity: this.granularity()
          }),
          valuation: this.reportService.getValuationByCategory()
        }).pipe(map((result) => ({ kind: 'summary' as const, ...result })));
      case 1:
        return forkJoin({
          valuation: this.reportService.getValuationByCategory()
        }).pipe(map((result) => ({ kind: 'valuation' as const, ...result })));
      case 2:
        return forkJoin({
          movements: this.reportService.getMovementsDetail({
            ...period,
            type: movementType === 'ALL' ? null : movementType,
            categoryId: this.categoryId(),
            limit: this.limit()
          }),
          timeseries: this.reportService.getMovementsTimeseries({
            ...period,
            granularity: this.granularity()
          })
        }).pipe(map((result) => ({ kind: 'movements' as const, ...result })));
      case 3:
        return forkJoin({
          topProducts: this.reportService.getTopProducts({
            ...period,
            metric: this.metric(),
            limit: this.limit()
          })
        }).pipe(map((result) => ({ kind: 'top' as const, ...result })));
      default:
        return forkJoin({
          lowStock: this.reportService.getLowStock()
        }).pipe(map((result) => ({ kind: 'lowStock' as const, ...result })));
    }
  }

  private applyResult(result: ReportResult): void {
    switch (result.kind) {
      case 'summary':
        this.summary.set(result.summary);
        this.timeseries.set(result.timeseries);
        this.valuation.set(result.valuation);
        break;
      case 'valuation':
        this.valuation.set(result.valuation);
        break;
      case 'movements':
        this.movements.set(result.movements);
        this.timeseries.set(result.timeseries);
        break;
      case 'top':
        this.topProducts.set(result.topProducts);
        break;
      case 'lowStock':
        this.lowStock.set(result.lowStock);
        break;
    }
  }

  private formatPeriodLabel(period: string): string {
    if (!period) {
      return '';
    }

    if (this.granularity() === 'MONTH') {
      const [year, month] = period.split('-');
      return month ? `${monthName(Number(month))} ${year?.slice(2)}` : period;
    }

    if (this.granularity() === 'WEEK') {
      const match = period.match(/(\d{4})-W(\d{1,2})/);
      return match ? `S${match[2]}` : period;
    }

    const match = period.match(/(\d{4})-(\d{2})-(\d{2})/);
    if (!match) {
      return period;
    }

    return `${match[3]} ${monthName(Number(match[2]))}`;
  }
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
