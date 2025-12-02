import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';

/**
 * @author Basile Fofack
 * @email juniorbasilefofack@gmail.com
 */
@Component({
  selector: 'app-message-modal-component',
  imports: [CommonModule],
  templateUrl: './message-modal-component.html',
  styleUrl: './message-modal-component.scss'
})
export class MessageModalComponent {

  public icon: string | undefined;
  public message: string | undefined;
  public title: string | undefined;
  public buttonText: string | undefined;
  public type: 'success' | 'danger' | 'warning' | 'info' = 'info';

  public modalService = inject(BsModalRef);

  constructor() { }

  get iconColorClass(): string {
    return `text-${this.type}`;
  }

  get buttonClass(): string {
    switch (this.type) {
      case 'success':
        return 'btn-success';
      case 'danger':
        return 'btn-danger';
      case 'warning':
        return 'btn-warning';
      default:
        return 'btn-info';
    }
  }

  get defaultIcon(): string {
    switch (this.type) {
      case 'success':
        return 'fa-check-circle';
      case 'danger':
        return 'fa-times-circle';
      case 'warning':
        return 'fa-exclamation-circle';
      default:
        return 'fa-info-circle';
    }
  }

  public close(): void {
    this.modalService.hide();
  }
}
