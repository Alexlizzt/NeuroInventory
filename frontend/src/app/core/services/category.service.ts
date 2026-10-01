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

  getCategories(query: PageQuery = {}): Observable> {
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

  createCategory(request: CreateCategoryRequest): Observable {
    return this.http.post(this.baseUrl, request);
  }

  updateCategory(id: string, request: UpdateCategoryRequest): Observable {
    return this.http.put(`\({this.baseUrl}/\){id}`, request);
  }
}