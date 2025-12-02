import { inject, Injectable } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { MessageModalComponent } from '../components/message-modal-component/message-modal-component';


@Injectable({
  providedIn: 'root',
})
export class NotificationService {

  private modalService = inject(BsModalService);
  constructor() { }

  public success(message: string): void {
    const initialState: Partial<MessageModalComponent> = {
      type: 'success',
      icon: 'fa-check-circle text-success',
      message: message
    };
    this.modalService.show(MessageModalComponent, { initialState, class: 'modal-success modal-sm' });
  }

  public warning(message: string): void {
    const initialState: Partial<MessageModalComponent> = {
      type: 'warning',
      icon: 'fa-warning text-warning',
      message: message
    };
    this.modalService.show(MessageModalComponent, { initialState, class: 'modal-warning modal-sm' });
  }

  public danger(message: string): void {
    const initialState: Partial<MessageModalComponent> = {
      type: 'danger',
      icon: 'fa-times-circle text-danger',
      message: message
    };
    this.modalService.show(MessageModalComponent, { initialState, class: 'modal-danger modal-sm' });
  }

  public info(message: string): void {
    const initialState: Partial<MessageModalComponent> = {
      type: 'info',
      icon: 'fa-info-circle text-info',
      message: message
    };
    this.modalService.show(MessageModalComponent, { initialState, class: 'modal-info modal-sm' });
  }
}
