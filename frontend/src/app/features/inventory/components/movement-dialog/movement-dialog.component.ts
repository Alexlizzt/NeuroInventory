import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ProductResponse } from '../../../../core/models/product.model';

export interface MovementDialogData {
  products: ProductResponse[];
  selectedProductId?: string;
}

@Component({
  selector: 'app-movement-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Registrar Movimiento de Inventario</h2>
    <form [formGroup]="movementForm" (ngSubmit)="onSubmit()">
      <mat-dialog-content class="form-content">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Producto</mat-label>
          <mat-select formControlName="productId">
            @for (prod of data.products; track prod.id) {
              <mat-option [value]="prod.id">{{ prod.name }} ({{ prod.sku }})</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Tipo de Movimiento</mat-label>
            <mat-select formControlName="type">
              <mat-option value="IN">Entrada (IN)</mat-option>
              <mat-option value="OUT">Salida (OUT)</mat-option>
              <mat-option value="ADJUSTMENT">Ajuste (ADJUSTMENT)</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Cantidad</mat-label>
            <input matInput type="number" formControlName="quantity" />
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Motivo / Observación</mat-label>
          <textarea matInput formControlName="reason" rows="2" placeholder="Ej: Compra a proveedor / Ajuste por auditoría"></textarea>
        </mat-form-field>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="onCancel()">Cancelar</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="movementForm.invalid">
          Registrar
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .form-content { display: flex; flex-direction: column; gap: 12px; min-width: 450px; padding-top: 8px; }
    .full-width { width: 100%; }
    .row { display: flex; gap: 16px; mat-form-field { flex: 1; } }
  `]
})
export class MovementDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<MovementDialogComponent>);
  public data = inject<MovementDialogData>(MAT_DIALOG_DATA);

  movementForm!: FormGroup;

  ngOnInit(): void {
    this.movementForm = this.fb.group({
      productId: [this.data.selectedProductId || '', Validators.required],
      type: ['IN', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      reason: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.movementForm.valid) {
      this.dialogRef.close(this.movementForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}