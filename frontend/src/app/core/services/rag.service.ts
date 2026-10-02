import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RagRequest, RagResponse } from '../models/rag.model';

@Injectable({
  providedIn: 'root'
})
export class RagService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/rag`;

  askQuestion(request: RagRequest): Observable<RagResponse> {
    return this.http.post<RagResponse>(`${this.baseUrl}/ask`, request);
  }
}