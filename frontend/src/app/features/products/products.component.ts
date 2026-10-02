import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ProductService } from '../../core/services/product.service';
import { ProductResponse } from '../../core/models/product.model';
import { ProductDialogComponent } from './components/product-dialog/product-dialog.component';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  displayedColumns: string[] = ['sku', 'name', 'price', 'active', 'actions'];
  dataSource: ProductResponse[] = [];

  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getProducts({ page: this.pageIndex, size: this.pageSize }).subscribe({
      next: (res) => {
        this.dataSource = res.content;
        this.totalElements = res.totalElements;
      },
      error: () => this.showSnackBar('Error al cargar productos')
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProducts();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ProductDialogComponent, { width: '500px' });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.productService.createProduct(result).subscribe({
          next: () => {
            this.showSnackBar('Producto creado exitosamente');
            this.loadProducts();
          },
          error: () => this.showSnackBar('Error al crear el producto')
        });
      }
    });
  }

  openEditDialog(product: ProductResponse): void {
    const dialogRef = this.dialog.open(ProductDialogComponent, {
      width: '500px',
      data: { product }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.productService.updateProduct(product.id, result).subscribe({
          next: () => {
            this.showSnackBar('Producto actualizado');
            this.loadProducts();
          },
          error: () => this.showSnackBar('Error al actualizar el producto')
        });
      }
    });
  }

  deleteProduct(id: string): void {
    if (confirm('¿Estás seguro de eliminar este producto?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => {
          this.showSnackBar('Producto eliminado');
          this.loadProducts();
        },
        error: () => this.showSnackBar('Error al eliminar el producto')
      });
    }
  }

  private showSnackBar(message: string): void {
    this.snackBar.open(message, 'Cerrar', { duration: 3000 });
  }
}