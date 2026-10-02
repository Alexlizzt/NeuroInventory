import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ProductResponse,
  CreateProductRequest,
  UpdateProductRequest,
  SemanticSearchProductResponse
} from '../models/product.model';
import { PageQuery, PageResult } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/products`;

  getProducts(query: PageQuery = {}): Observable<PageResult<ProductResponse>> {
    let params = new HttpParams()
      .set('page', (query.page ?? 0).toString())
      .set('size', (query.size ?? 10).toString())
      .set('sortBy', query.sortBy ?? 'name')
      .set('direction', query.direction ?? 'ASC');

    return this.http.get<PageResult<ProductResponse>>(this.baseUrl, { params });
  }

  getById(id: string): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.baseUrl}/${id}`);
  }

  createProduct(request: CreateProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.baseUrl, request);
  }

  updateProduct(id: string, request: UpdateProductRequest): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.baseUrl}/${id}`, request);
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  searchSemantically(query: string, limit: number = 5): Observable<SemanticSearchProductResponse[]> {
    const params = new HttpParams()
      .set('query', query)
      .set('limit', limit.toString());

    return this.http.get<SemanticSearchProductResponse[]>(`${this.baseUrl}/search/semantic`, { params });
  }
}
