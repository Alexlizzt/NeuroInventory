import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

export type KpiTone = 'primary' | 'success' | 'warning' | 'danger' | 'info';

/**
 * Tarjeta de indicador clave (KPI) reutilizable del dashboard.
 */
@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatTooltipModule],
  templateUrl: './kpi-card.component.html',
  styleUrl: './kpi-card.component.scss'
})
export class KpiCardComponent {
  readonly label = input.required<string>();
  readonly value = input<string | number>('');
  readonly icon = input('insights');
  readonly hint = input('');
  readonly tone = input<KpiTone>('primary');
}
