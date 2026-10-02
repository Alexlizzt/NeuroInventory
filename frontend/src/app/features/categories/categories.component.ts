import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { CategoryService } from '../../core/services/category.service';
import { CategoryResponse } from '../../core/models/category.model';
import { CategoryDialogComponent } from './components/category-dialog/category-dialog.component';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.scss'
})
export class CategoriesComponent implements OnInit {
  private categoryService = inject(CategoryService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  displayedColumns: string[] = ['name', 'description', 'actions'];
  dataSource: CategoryResponse[] = [];
  
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getCategories({ page: this.pageIndex, size: this.pageSize }).subscribe({
      next: (res) => {
        this.dataSource = res.content;
        this.totalElements = res.totalElements;
      },
      error: () => this.showSnackBar('Error al cargar categorías')
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCategories();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent);
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.categoryService.createCategory(result).subscribe({
          next: () => {
            this.showSnackBar('Categoría creada exitosamente');
            this.loadCategories();
          },
          error: () => this.showSnackBar('Error al crear la categoría')
        });
      }
    });
  }

  openEditDialog(category: CategoryResponse): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent, { data: { category } });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.categoryService.updateCategory(category.id, result).subscribe({
          next: () => {
            this.showSnackBar('Categoría actualizada');
            this.loadCategories();
          },
          error: () => this.showSnackBar('Error al actualizar la categoría')
        });
      }
    });
  }

  private showSnackBar(message: string): void {
    this.snackBar.open(message, 'Cerrar', { duration: 3000 });
  }
}