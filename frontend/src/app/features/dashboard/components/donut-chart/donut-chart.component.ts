import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CHART_PALETTE, ChartSegment } from '../../../../core/models/chart.model';

interface DonutArc extends ChartSegment {
  color: string;
  dashArray: string;
  dashOffset: number;
  percentage: number;
}

/**
 * Gráfico circular (donut) implementado con SVG nativo.
 */
@Component({
  selector: 'app-donut-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './donut-chart.component.html',
  styleUrl: './donut-chart.component.scss'
})
export class DonutChartComponent {
  readonly segments = input<ChartSegment[]>([]);
  readonly centerLabel = input('Total');
  readonly centerValueFormatter = input<(value: number) => string>((value) =>
    value.toLocaleString('es-ES')
  );
  readonly legendValueFormatter = input<(value: number) => string>((value) =>
    value.toLocaleString('es-ES')
  );

  readonly radius = 70;
  readonly strokeWidth = 26;
  readonly circumference = 2 * Math.PI * 70;

  readonly total = computed(() =>
    this.segments().reduce((accumulator, segment) => accumulator + (segment.value ?? 0), 0)
  );

  readonly arcs = computed<DonutArc[]>(() => {
    const total = this.total();
    if (total <= 0) {
      return [];
    }

    let offset = 0;

    return this.segments()
      .filter((segment) => (segment.value ?? 0) > 0)
      .map((segment, index) => {
        const fraction = segment.value / total;
        const length = fraction * this.circumference;

        const arc: DonutArc = {
          ...segment,
          color: segment.color ?? CHART_PALETTE[index % CHART_PALETTE.length],
          dashArray: `${length} ${this.circumference - length}`,
          dashOffset: -offset,
          percentage: fraction * 100
        };

        offset += length;
        return arc;
      });
  });
}
