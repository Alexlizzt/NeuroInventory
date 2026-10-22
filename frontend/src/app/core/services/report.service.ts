import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CategoryStockReportResponse,
  DashboardSummaryResponse,
  LowStockReportResponse,
  MovementDetailReportResponse,
  MovementPeriodReportResponse,
  ProductValuationReportResponse,
  ReportFilter
} from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/reports`;

  getDashboardSummary(filter: ReportFilter = {}): Observable<DashboardSummaryResponse> {
    return this.http.get<DashboardSummaryResponse>(`${this.baseUrl}/dashboard`, {
      params: this.buildPeriodParams(filter)
    });
  }

  getValuationByCategory(): Observable<CategoryStockReportResponse[]> {
    return this.http.get<CategoryStockReportResponse[]>(`${this.baseUrl}/valuation/categories`);
  }

  getMovementsTimeseries(filter: ReportFilter = {}): Observable<MovementPeriodReportResponse[]> {
    const params = this.buildPeriodParams(filter).set(
      'granularity',
      filter.granularity ?? 'DAY'
    );

    return this.http.get<MovementPeriodReportResponse[]>(`${this.baseUrl}/movements/timeseries`, {
      params
    });
  }

  getMovementsDetail(filter: ReportFilter = {}): Observable<MovementDetailReportResponse[]> {
    let params = this.buildPeriodParams(filter);

    if (filter.type) {
      params = params.set('type', filter.type);
    }
    if (filter.categoryId) {
      params = params.set('categoryId', filter.categoryId);
    }
    if (filter.limit) {
      params = params.set('limit', filter.limit.toString());
    }

    return this.http.get<MovementDetailReportResponse[]>(`${this.baseUrl}/movements/detail`, {
      params
    });
  }

  getTopProducts(filter: ReportFilter = {}): Observable<ProductValuationReportResponse[]> {
    let params = this.buildPeriodParams(filter).set('metric', filter.metric ?? 'VALUE');

    if (filter.limit) {
      params = params.set('limit', filter.limit.toString());
    }

    return this.http.get<ProductValuationReportResponse[]>(`${this.baseUrl}/products/top`, {
      params
    });
  }

  getLowStock(): Observable<LowStockReportResponse[]> {
    return this.http.get<LowStockReportResponse[]>(`${this.baseUrl}/stock/low`);
  }

  private buildPeriodParams(filter: ReportFilter): HttpParams {
    let params = new HttpParams();

    if (filter.from) {
      params = params.set('from', filter.from);
    }
    if (filter.to) {
      params = params.set('to', filter.to);
    }

    return params;
  }
}
