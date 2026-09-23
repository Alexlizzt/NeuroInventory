import { Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { NavigationItem } from '../navigation-item';

@Component({
  selector: 'app-navigation-item',
  imports: [
    RouterLink,
    RouterLinkActive,
  ],
  templateUrl: './navigation-item.html',
  styleUrl: './navigation-item.scss',
})
export class NavigationItemComponent {

  readonly item = input.required<NavigationItem>();

}