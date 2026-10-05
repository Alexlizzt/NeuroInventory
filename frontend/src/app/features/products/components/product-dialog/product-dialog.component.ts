import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';

import { CategoryService } from '../../../../core/services/category.service';
import { CategoryResponse } from '../../../../core/models/category.model';
import { ProductResponse } from '../../../../core/models/product.model';

export interface ProductDialogData {
  product?: ProductResponse;
}

@Component({
  selector: 'app-product-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatIconModule
  ],
  templateUrl: './product-dialog.component.html',
  styleUrl: './product-dialog.component.scss'
})
export class ProductDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private categoryService = inject(CategoryService);
  private dialogRef = inject(MatDialogRef<ProductDialogComponent>);
  public data = inject<ProductDialogData>(MAT_DIALOG_DATA);

  productForm!: FormGroup;
  categories = signal<CategoryResponse[]>([]);
  isEditMode = false;

  manualContent = '';
  manualFileName = '';

  onManualFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.manualFileName = file.name;
    const reader = new FileReader();
    reader.onload = () => (this.manualContent = (reader.result as string) ?? '');
    reader.readAsText(file);
  }

  ngOnInit(): void {
    this.isEditMode = !!this.data?.product;
    this.loadCategories();
    this.initForm();
  }

  private initForm(): void {
    const prod = this.data?.product;
    this.productForm = this.fb.group({
      categoryId: [prod?.categoryId || '', Validators.required],
      sku: [prod?.sku || '', [Validators.required, Validators.maxLength(50)]],
      name: [prod?.name || '', [Validators.required, Validators.maxLength(100)]],
      description: [prod?.description || ''],
      price: [prod?.price ?? 0, [Validators.required, Validators.min(0)]],
      initialStock: [{ value: 0, disabled: this.isEditMode }, [Validators.required, Validators.min(0)]],
      minStock: [{ value: 5, disabled: this.isEditMode }, [Validators.required, Validators.min(0)]],
      active: [prod?.active ?? true]
    });
  }

  private loadCategories(): void {
    this.categoryService.getCategories({ page: 0, size: 100 }).subscribe({
      next: (res) => (this.categories.set(res.content)),
      error: (err) => console.error('Error cargando categorías:', err)
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) return;
    this.dialogRef.close({
      ...this.productForm.getRawValue(),
      manualContent: this.manualContent,
      manualFileName: this.manualFileName
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}