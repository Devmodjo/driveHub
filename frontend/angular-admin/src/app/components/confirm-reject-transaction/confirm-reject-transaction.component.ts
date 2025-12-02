import { Component } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-confirm-delete-modal',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="modal-content rounded-4 shadow-lg">
      <div class="modal-header border-0 pb-0">
        <h4 class="modal-title fw-bold text-danger">
          <i class="fa fa-exclamation-triangle me-2"></i>
          Confirmation d'annulation
        </h4>
        <button type="button" class="btn-close shadow-none" aria-label="Close" (click)="decline()"></button>
      </div>

      <div class="modal-body py-4">
        <div class="d-flex flex-column align-items-center">
          <div class="message-wrapper text-center">
            <p class="message-text">
              {{ message }}
            </p>
          </div>
        </div>
      </div>

      <div class="modal-footer border-0 pt-0">
        <button type="button" class="btn btn-outline-secondary" (click)="decline()">
          Annuler
        </button>
        <button type="button" class="btn btn-danger" (click)="confirm()">
          Confirmer
        </button>
      </div>
    </div>
  `,
    styles: [`
    .modal-title {
      font-size: 1.5rem;
      color: #e74c3c;
    }

    .message-text {
      font-size: 1.1rem;
      line-height: 1.5;
      color: #4a5568;
      margin: 0;
    }

    .btn {
      min-width: 120px;
      font-weight: 500;
      transition: all 0.2s;
    }

    .btn:hover {
      transform: translateY(-1px);
    }

    .btn:active {
      transform: translateY(1px);
    }

    .btn-danger {
      background-color: #e74c3c;
      border-color: #e74c3c;
    }

    .btn-danger:hover {
      background-color: darken(#e74c3c, 5%);
      border-color: darken(#e74c3c, 5%);
    }
  `]
})
export class ConfirmRejetTransactionModalComponent {
    message?: string;
    onClose?: (confirmed: boolean) => void;

    constructor(public modalRef: BsModalRef) { }

    confirm(): void {
        if (this.onClose) {
            this.onClose(true);
        }
        this.modalRef.hide();
    }

    decline(): void {
        if (this.onClose) {
            this.onClose(false);
        }
        this.modalRef.hide();
    }
}
