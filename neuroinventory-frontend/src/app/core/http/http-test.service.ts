import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class HttpTestService {

  private readonly http = inject(HttpClient);

  test(): void {
    this.http.get('/products').subscribe({
      next: response => {
        console.log('HTTP response:', response);
      },
      error: error => {
        console.error('HTTP error:', error);
      },
    });
  }
}