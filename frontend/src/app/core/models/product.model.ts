export interface ProductResponse {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  active: boolean;
  categoryId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  categoryId: string;
  sku: string;
  name: string;
  description?: string;
  price: number;
  initialStock: number;
  minStock: number;
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  price?: number;
  categoryId?: string;
  active?: boolean;
}

export interface SemanticSearchProductResponse {
  id: string;
  categoryId: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  active: boolean;
  score: number;
  rationale: string;
}