import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <header class="page-header">
      <h1>{{ title }}</h1>
      @if (subtitle) {
        <p>{{ subtitle }}</p>
      }
    </header>
  `,
})
export class PageHeaderComponent {
  @Input({ required: true }) title!: string;
  @Input() subtitle = '';
}
