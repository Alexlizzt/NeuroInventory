import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RagRequest, RagResponse, RagIngestRequest, RagIngestResponse } from '../models/rag.model';

@Injectable({
  providedIn: 'root'
})
export class RagService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ai/rag`;

  askQuestion(request: RagRequest): Observable<RagResponse> {
    return this.http.post<RagResponse>(`${this.baseUrl}/query`, request);
  }

  ingestDocument(request: RagIngestRequest): Observable<RagIngestResponse> {
    return this.http.post<RagIngestResponse>(`${this.baseUrl}/documents`, request);
  }
}