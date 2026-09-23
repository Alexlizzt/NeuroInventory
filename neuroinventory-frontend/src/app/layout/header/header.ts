import { Component } from '@angular/core';
import { HeaderUser } from './header-user';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  protected readonly user: HeaderUser = {
    name: 'Usuario',
    email: 'usuario@example.com',
  };
}
