import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  RegisterMovementRequest,
  InventoryMovementResponse,
  StockResponse
} from '../models/inventory.model';
import { PageQuery, PageResult } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/inventory`;

  registerMovement(request: RegisterMovementRequest): Observable<InventoryMovementResponse> {
    return this.http.post<InventoryMovementResponse>(`${this.baseUrl}/movements`, request);
  }

  getMovementsByProduct(productId: string, query: PageQuery = {}): Observable<PageResult<InventoryMovementResponse>> {
    let params = new HttpParams()
      .set('page', (query.page ?? 0).toString())
      .set('size', (query.size ?? 10).toString())
      .set('sortBy', query.sortBy ?? 'createdAt')
      .set('direction', query.direction ?? 'DESC');

    return this.http.get<PageResult<InventoryMovementResponse>>(`${this.baseUrl}/movements/product/${productId}`, { params });
  }

  getStock(productId: string): Observable<StockResponse> {
    return this.http.get<StockResponse>(`${this.baseUrl}/stock/${productId}`);
  }
}