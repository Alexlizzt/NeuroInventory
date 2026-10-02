import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest
} from '../models/category.model';
import { PageQuery, PageResult } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/categories`;

  getCategories(query: PageQuery = {}): Observable<PageResult<CategoryResponse>> {
    let params = new HttpParams()
      .set('page', (query.page ?? 0).toString())
      .set('size', (query.size ?? 10).toString())
      .set('sortBy', query.sortBy ?? 'name')
      .set('direction', query.direction ?? 'ASC');

    return this.http.get<PageResult<CategoryResponse>>(this.baseUrl, { params });
  }

  getById(id: string): Observable<CategoryResponse> {
    return this.http.get<CategoryResponse>(`${this.baseUrl}/${id}`);
  }

  createCategory(request: CreateCategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(this.baseUrl, request);
  }

  updateCategory(id: string, request: UpdateCategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${this.baseUrl}/${id}`, request);
  }
}
