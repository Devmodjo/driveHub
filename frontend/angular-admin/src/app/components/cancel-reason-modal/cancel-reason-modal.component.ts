import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-cancel-reason-modal',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    template: `
    <div class="modal-header">
      <h4 class="modal-title">Annulation du rendez-vous</h4>
      <button type="button" class="btn-close" aria-label="Close" (click)="onCancel()">
        <span aria-hidden="true">&times;</span>
      </button>
    </div>

    <div class="modal-body">
      <p><strong>{{ message }}</strong></p>
      
      <form [formGroup]="cancelForm" (ngSubmit)="onConfirm()">
        <div class="mb-3">
          <label for="reason" class="form-label">
            Raison de l'annulation <span class="text-danger">*</span>
          </label>
          <textarea 
            id="reason"
            formControlName="reason"
            class="form-control"
            [class.is-invalid]="cancelForm.get('reason')?.invalid && cancelForm.get('reason')?.touched"
            rows="4"
            placeholder="Veuillez préciser la raison de l'annulation..."
            maxlength="500">
          </textarea>
          <div class="invalid-feedback" *ngIf="cancelForm.get('reason')?.invalid && cancelForm.get('reason')?.touched">
            <small *ngIf="cancelForm.get('reason')?.errors?.['required']">
              La raison de l'annulation est obligatoire.
            </small>
            <small *ngIf="cancelForm.get('reason')?.errors?.['minlength']">
              La raison doit contenir au moins 10 caractères.
            </small>
          </div>
          <small class="form-text text-muted">
            {{ cancelForm.get('reason')?.value?.length || 0 }}/500 caractères
          </small>
        </div>
      </form>
    </div>

    <div class="modal-footer">
      <button type="button" class="btn btn-secondary" (click)="onCancel()">
        <i class="fa fa-times me-1"></i>
        Fermer
      </button>
      <button 
        type="button" 
        class="btn btn-danger" 
        (click)="onConfirm()"
        [disabled]="cancelForm.invalid || isSubmitting">
        <span *ngIf="isSubmitting" class="spinner-border spinner-border-sm me-2" role="status"></span>
        <i *ngIf="!isSubmitting" class="fa fa-ban me-1"></i>
        {{ isSubmitting ? submittingText : confirmText }}
      </button>
    </div>
  `,
    styles: [`
    .modal-header {
      background-color: #f8f9fa;
      border-bottom: 1px solid #dee2e6;
    }
    
    .modal-title {
      color: #dc3545;
      font-weight: 600;
    }
    
    .form-label {
      font-weight: 500;
    }
    
    .btn-danger {
      background-color: #dc3545;
      border-color: #dc3545;
    }
    
    .btn-danger:hover {
      background-color: #c82333;
      border-color: #bd2130;
    }
    
    .text-danger {
      color: #dc3545 !important;
    }
  `]
})
export class CancelReasonModalComponent implements OnInit {
    cancelForm!: FormGroup;
    message: string = '';
    isSubmitting = false;
    confirmText = "Confirmer l'annulation";
    submittingText = 'Annulation...';
    onClose?: (confirmed: boolean, reason?: string) => void;

    constructor(
        public fb: FormBuilder,
        public bsModalRef: BsModalRef
    ) { }

    ngOnInit(): void {
        this.initForm();
    }

    private initForm(): void {
        this.cancelForm = this.fb.group({
            reason: ['', [
                Validators.required,
                Validators.minLength(10),
                Validators.maxLength(500)
            ]]
        });
    }

    onConfirm(): void {
        if (this.cancelForm.valid) {
            const reason = this.cancelForm.get('reason')?.value.trim();
            this.isSubmitting = true;

            setTimeout(() => {
                this.onClose?.(true, reason);
                this.bsModalRef.hide();
            }, 300);
        } else {
            Object.keys(this.cancelForm.controls).forEach(key => {
                this.cancelForm.get(key)?.markAsTouched();
            });
        }
    }

    onCancel(): void {
        this.onClose?.(false);
        this.bsModalRef.hide();
    }
}