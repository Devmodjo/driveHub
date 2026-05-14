import { Component } from '@angular/core';
import { Input } from '@angular/core';

@Component({
  selector: 'register-modal',
  imports: [],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
})
export class ModalComponent {

  @Input() title?: string;
  @Input() message?:string;
  @Input() element?: HTMLElement;

}
