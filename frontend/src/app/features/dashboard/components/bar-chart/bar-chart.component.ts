import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CHART_PALETTE, ChartBar } from '../../../../core/models/chart.model';

interface RenderedBar extends ChartBar {
  x: number;
  y: number;
  width: number;
  height: number;
  color: string;
  label: string;
}

/**
 * Gráfico de barras vertical implementado con SVG nativo.
 */
@Component({
  selector: 'app-bar-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bar-chart.component.html',
  styleUrl: './bar-chart.component.scss'
})
export class BarChartComponent {
  readonly bars = input<ChartBar[]>([]);
  readonly valueFormatter = input<(value: number) => string>((value) =>
    value.toLocaleString('es-ES')
  );
  readonly labelFormatter = input<(label: string) => string>((label) => truncate(label, 14));

  readonly width = 680;
  readonly height = 280;
  readonly padding = { top: 26, right: 16, bottom: 52, left: 58 };

  private readonly innerWidth = this.width - this.padding.left - this.padding.right;
  private readonly innerHeight = this.height - this.padding.top - this.padding.bottom;

  readonly maxValue = computed(() => {
    const max = this.bars().reduce((accumulator, bar) => Math.max(accumulator, bar.value ?? 0), 0);
    return max > 0 ? max : 1;
  });

  readonly renderedBars = computed<RenderedBar[]>(() => {
    const bars = this.bars();
    const count = Math.max(bars.length, 1);
    const band = this.innerWidth / count;
    const barWidth = Math.min(band * 0.55, 56);
    const max = this.maxValue();

    return bars.map((bar, index) => {
      const height = max > 0 ? (bar.value / max) * this.innerHeight : 0;

      return {
        ...bar,
        x: this.padding.left + band * index + (band - barWidth) / 2,
        y: this.padding.top + this.innerHeight - height,
        width: barWidth,
        height,
        color: bar.color ?? CHART_PALETTE[index % CHART_PALETTE.length],
        label: this.labelFormatter()(bar.label)
      };
    });
  });

  readonly baselineY = this.padding.top + this.innerHeight;
}

/**
 * Recorta las etiquetas largas para que no se desborden del gráfico.
 */
function truncate(value: string, maxLength: number): string {
  if (!value) {
    return '';
  }
  return value.length > maxLength ? `${value.slice(0, maxLength - 1)}…` : value;
}
