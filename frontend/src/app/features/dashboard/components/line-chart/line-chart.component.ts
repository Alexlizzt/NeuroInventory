import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ChartSeries } from '../../../../core/models/chart.model';

interface LinePoint {
  x: number;
  y: number;
  value: number;
  label: string;
}

interface RenderedSeries extends ChartSeries {
  polyline: string;
  dots: LinePoint[];
}

interface GridLine {
  y: number;
  label: string;
}

interface AxisLabel {
  x: number;
  text: string;
}

/**
 * Gráfico de líneas (series temporales) implementado con SVG nativo.
 */
@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './line-chart.component.html',
  styleUrl: './line-chart.component.scss'
})
export class LineChartComponent {
  readonly labels = input<string[]>([]);
  readonly series = input<ChartSeries[]>([]);
  readonly valueFormatter = input<(value: number) => string>((value) =>
    value.toLocaleString('es-ES')
  );

  readonly width = 680;
  readonly height = 280;
  readonly padding = { top: 18, right: 20, bottom: 42, left: 58 };

  private readonly innerWidth = this.width - this.padding.left - this.padding.right;
  private readonly innerHeight = this.height - this.padding.top - this.padding.bottom;

  readonly maxValue = computed(() => {
    const values = this.series().flatMap((serie) => serie.points ?? []);
    const max = values.reduce((accumulator, value) => Math.max(accumulator, value ?? 0), 0);
    return niceCeiling(max);
  });

  readonly gridLines = computed<GridLine[]>(() => {
    const max = this.maxValue();
    const steps = 4;

    return Array.from({ length: steps + 1 }, (_, index) => {
      const ratio = index / steps;
      const value = max * (1 - ratio);

      return {
        y: this.padding.top + this.innerHeight * ratio,
        label: this.valueFormatter()(value)
      };
    });
  });

  readonly renderedSeries = computed<RenderedSeries[]>(() => {
    const labels = this.labels();
    const count = Math.max(labels.length, 1);
    const max = this.maxValue();

    return this.series().map((serie) => {
      const points = serie.points ?? [];

      const dots: LinePoint[] = points.map((value, index) => ({
        x: this.xFor(index, count),
        y: this.yFor(value, max),
        value,
        label: labels[index] ?? ''
      }));

      return {
        ...serie,
        polyline: dots.map((dot) => `${dot.x},${dot.y}`).join(' '),
        dots
      };
    });
  });

  readonly axisLabels = computed<AxisLabel[]>(() => {
    const labels = this.labels();
    if (labels.length === 0) {
      return [];
    }

    const maxLabels = 10;
    const step = Math.max(1, Math.ceil(labels.length / maxLabels));
    const count = Math.max(labels.length, 1);

    return labels
      .map((text, index) => ({ text, index }))
      .filter((entry) => entry.index % step === 0)
      .map((entry) => ({
        text: entry.text,
        x: this.xFor(entry.index, count)
      }));
  });

  private xFor(index: number, count: number): number {
    return this.padding.left + (index + 0.5) * (this.innerWidth / count);
  }

  private yFor(value: number, max: number): number {
    if (max <= 0) {
      return this.padding.top + this.innerHeight;
    }
    return this.padding.top + this.innerHeight - (value / max) * this.innerHeight;
  }
}

/**
 * Redondea el valor máximo del gráfico a un múltiplo legible (1, 2 o 5 x 10^n).
 */
function niceCeiling(value: number): number {
  if (!value || value <= 0) {
    return 1;
  }

  const exponent = Math.floor(Math.log10(value));
  const magnitude = Math.pow(10, exponent);
  const normalized = value / magnitude;

  const step = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10;

  return step * magnitude;
}
