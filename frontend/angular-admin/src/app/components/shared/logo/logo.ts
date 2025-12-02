import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-logo',
  templateUrl: './logo.html',
  styleUrl: './logo.scss'
})
export class Logo {

  @Input() width: string = '100';
  @Input() height: string = '40';
  @Input() fill: string = 'white';
  @Input() class: string = '';
  @Input() viewBox: string = '0 0 300 293';
}
