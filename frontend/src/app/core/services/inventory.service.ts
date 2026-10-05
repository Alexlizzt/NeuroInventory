import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  RegisterMovementRequest,
  InventoryMovementResponse,
  StockResponse
} from '../models/inventory.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/inventory`;

  registerMovement(request: RegisterMovementRequest): Observable<InventoryMovementResponse> {
    return this.http.post<InventoryMovementResponse>(`${this.baseUrl}/movements`, request);
  }

  getMovementsByProduct(productId: string): Observable<InventoryMovementResponse[]> {
    return this.http.get<InventoryMovementResponse[]>(`${this.baseUrl}/movements/history/${productId}`);
  }

  getStock(productId: string): Observable<StockResponse> {
    return this.http.get<StockResponse>(`${this.baseUrl}/stock/${productId}`);
  }
}