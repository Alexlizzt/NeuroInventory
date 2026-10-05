import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { InventoryService } from '../../core/services/inventory.service';
import { ProductService } from '../../core/services/product.service';
import { ProductResponse } from '../../core/models/product.model';
import { InventoryMovementResponse, StockResponse } from '../../core/models/inventory.model';
import { MovementDialogComponent } from './components/movement-dialog/movement-dialog.component';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class InventoryComponent implements OnInit {
  private inventoryService = inject(InventoryService);
  private productService = inject(ProductService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  products = signal<ProductResponse[]>([]);
  selectedProductId: string = '';
  stockInfo = signal<StockResponse | undefined>(undefined);

  displayedColumns: string[] = ['type', 'quantity', 'reason', 'userId', 'createdAt'];
  dataSource = signal<InventoryMovementResponse[]>([]);

  totalElements = signal(0);
  pageSize = 10;
  pageIndex = 0;

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getProducts({ page: 0, size: 100 }).subscribe({
      next: (res) => {
        this.products.set(res.content);
        const products = this.products();
        if (products.length > 0) {
          this.selectedProductId = products[0].id;
          this.onProductChange();
        }
      },
      error: () => this.showSnackBar('Error al cargar productos')
    });
  }

  onProductChange(): void {
    if (!this.selectedProductId) return;
    this.pageIndex = 0;
    this.loadStock();
    this.loadMovements();
  }

  loadStock(): void {
    this.inventoryService.getStock(this.selectedProductId).subscribe({
      next: (stock) => (this.stockInfo.set(stock)),
      error: () => (this.stockInfo.set(undefined))
    });
  }

  loadMovements(): void {
    this.inventoryService.getMovementsByProduct(this.selectedProductId, {
      page: this.pageIndex,
      size: this.pageSize
    }).subscribe({
      next: (res) => {
        this.dataSource.set(res.content);
        this.totalElements.set(res.totalElements);
      },
      error: () => this.showSnackBar('Error al cargar el historial de movimientos')
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadMovements();
  }

  openMovementDialog(): void {
    const dialogRef = this.dialog.open(MovementDialogComponent, {
      data: {
        products: this.products(),
        selectedProductId: this.selectedProductId
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.inventoryService.registerMovement(result).subscribe({
          next: () => {
            this.showSnackBar('Movimiento registrado exitosamente');
            this.loadStock();
            this.loadMovements();
          },
          error: () => this.showSnackBar('Error al registrar el movimiento')
        });
      }
    });
  }

  private showSnackBar(message: string): void {
    this.snackBar.open(message, 'Cerrar', { duration: 3000 });
  }
}