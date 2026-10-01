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

  getProducts(query: PageQuery = {}): Observable> {
    let params = new HttpParams()
      .set('page', (query.page ?? 0).toString())
      .set('size', (query.size ?? 10).toString())
      .set('sortBy', query.sortBy ?? 'name')
      .set('direction', query.direction ?? 'ASC');

    return this.http.get>(this.baseUrl, { params });
  }

  getById(id: string): Observable {
    return this.http.get(`\({this.baseUrl}/\){id}`);
  }

  createProduct(request: CreateProductRequest): Observable {
    return this.http.post(this.baseUrl, request);
  }

  updateProduct(id: string, request: UpdateProductRequest): Observable {
    return this.http.put(`\({this.baseUrl}/\){id}`, request);
  }

  deleteProduct(id: string): Observable {
    return this.http.delete(`\({this.baseUrl}/\){id}`);
  }

  searchSemantically(query: string, limit: number = 5): Observable {
    const params = new HttpParams()
      .set('query', query)
      .set('limit', limit.toString());

    return this.http.get(`${this.baseUrl}/search/semantic`, { params });
  }
}