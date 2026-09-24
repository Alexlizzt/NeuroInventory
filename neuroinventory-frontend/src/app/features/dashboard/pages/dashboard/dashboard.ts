import { Component, inject } from '@angular/core';

import { HttpTestService } from '../../../../core/http/http-test.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly httpTestService = inject(HttpTestService);

  protected testHttp(): void {
    this.httpTestService.test();
  }
}