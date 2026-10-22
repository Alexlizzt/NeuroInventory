/**
 * Utilidades para exportar reportes del frontend a formato CSV.
 */

export interface CsvColumn<T> {
  header: string;
  value: (row: T) => string | number | null | undefined;
}

/**
 * Descarga un conjunto de datos como archivo CSV.
 */
export function downloadCsv<T>(filename: string, columns: CsvColumn<T>[], rows: T[]): void {
  const header = columns.map((column) => escapeCsvValue(column.header)).join(',');

  const body = rows
    .map((row) => columns.map((column) => escapeCsvValue(column.value(row))).join(','))
    .join('\n');

  const csv = rows.length > 0 ? `${header}\n${body}` : header;

  // El BOM permite que Excel reconozca la codificación UTF-8.
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = url;
  link.download = filename.endsWith('.csv') ? filename : `${filename}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

/**
 * Escapa el valor de una celda y neutraliza fórmulas para evitar
 * la inyección de código al abrir el archivo en una hoja de cálculo.
 */
function escapeCsvValue(value: string | number | null | undefined): string {
  if (value === null || value === undefined) {
    return '';
  }

  const raw = String(value);

  const sanitized = /^[=+\-@]/.test(raw) ? `'${raw}` : raw;
  const escaped = sanitized.replace(/"/g, '""');

  return /[",\n\r]/.test(escaped) ? `"${escaped}"` : escaped;
}
