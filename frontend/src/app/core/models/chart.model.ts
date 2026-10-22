/**
 * Segmento de un gráfico circular (donut).
 */
export interface ChartSegment {
  label: string;
  value: number;
  color?: string;
}

/**
 * Barra de un gráfico de barras.
 */
export interface ChartBar {
  label: string;
  value: number;
  color?: string;
}

/**
 * Serie de datos de un gráfico de líneas.
 */
export interface ChartSeries {
  name: string;
  color: string;
  points: number[];
}

/**
 * Paleta por defecto compartida por los gráficos del dashboard.
 */
export const CHART_PALETTE = [
  '#3f51b5',
  '#009688',
  '#ff9800',
  '#9c27b0',
  '#f44336',
  '#00acc1',
  '#8bc34a',
  '#795548',
  '#607d8b',
  '#ffc107'
];
