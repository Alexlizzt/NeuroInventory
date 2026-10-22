export type ReportGranularity = 'DAY' | 'WEEK' | 'MONTH';

export type ReportMetric = 'VALUE' | 'MOVEMENT';

export type StockSeverity = 'CRITICAL' | 'WARNING' | 'HEALTHY';

export type MovementTypeFilter = 'IN' | 'OUT' | 'ADJUSTMENT';

export interface DashboardSummaryResponse {
  totalProducts: number;
  activeProducts: number;
  totalCategories: number;
  totalUnits: number;
  lowStockProducts: number;
  outOfStockProducts: number;
  inventoryValue: number;
  inboundMovements: number;
  outboundMovements: number;
  inboundUnits: number;
  outboundUnits: number;
  netUnits: number;
  periodStart: string;
  periodEnd: string;
  generatedAt: string;
}

export interface CategoryStockReportResponse {
  categoryId: string;
  categoryName: string;
  productCount: number;
  totalUnits: number;
  totalValue: number;
  percentage: number;
  lowStockCount: number;
}

export interface MovementPeriodReportResponse {
  period: string;
  inboundUnits: number;
  outboundUnits: number;
  adjustmentUnits: number;
  netUnits: number;
  inboundMovements: number;
  outboundMovements: number;
  inboundValue: number;
  outboundValue: number;
  netValue: number;
}

export interface ProductValuationReportResponse {
  productId: string;
  sku: string;
  productName: string;
  categoryId: string;
  categoryName: string;
  quantity: number;
  minStock: number;
  unitPrice: number;
  totalValue: number;
  movementCount: number;
}

export interface MovementDetailReportResponse {
  movementId: string;
  productId: string;
  sku: string;
  productName: string;
  categoryId: string;
  categoryName: string;
  type: MovementTypeFilter;
  quantity: number;
  unitPrice: number;
  totalValue: number;
  reason: string | null;
  userId: string | null;
  createdAt: string;
}

export interface LowStockReportResponse {
  productId: string;
  sku: string;
  productName: string;
  categoryName: string;
  quantity: number;
  minStock: number;
  shortage: number;
  coveragePercentage: number;
  unitPrice: number;
  totalValue: number;
  suggestedPurchaseValue: number;
  severity: StockSeverity;
}

/**
 * Filtros aplicados a los reportes de inventario.
 */
export interface ReportFilter {
  from?: string | null;
  to?: string | null;
  granularity?: ReportGranularity;
  metric?: ReportMetric;
  type?: MovementTypeFilter | null;
  categoryId?: string | null;
  limit?: number;
}
