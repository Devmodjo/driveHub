// angular import
import { Component, inject, OnInit } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';

// bootstrap import
import { NgbDropdownConfig } from '@ng-bootstrap/ng-bootstrap';

// project import
import { SharedModule } from 'src/app/theme/shared/shared.module';
import { ChatUserListComponent } from './chat-user-list/chat-user-list.component';
import { AuthenticationService } from 'src/app/services/authentication.service';

@Component({
  selector: 'app-nav-right',
  imports: [SharedModule, ChatUserListComponent],
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss'],
  providers: [NgbDropdownConfig],
  animations: [
    trigger('slideInOutLeft', [
      transition(':enter', [style({ transform: 'translateX(100%)' }), animate('300ms ease-in', style({ transform: 'translateX(0%)' }))]),
      transition(':leave', [animate('300ms ease-in', style({ transform: 'translateX(100%)' }))])
    ]),
    trigger('slideInOutRight', [
      transition(':enter', [style({ transform: 'translateX(-100%)' }), animate('300ms ease-in', style({ transform: 'translateX(0%)' }))]),
      transition(':leave', [animate('300ms ease-in', style({ transform: 'translateX(-100%)' }))])
    ])
  ]
})
export class NavRightComponent implements OnInit {
  // public props
  visibleUserList: boolean;
  chatMessage: boolean;
  friendId!: number;
  user!: any;

  public authService = inject(AuthenticationService);

  // constructor
  constructor() {
    this.visibleUserList = false;
    this.chatMessage = false;
  }

  // public method
  // eslint-disable-next-line
  onChatToggle(friendID: any) {
    this.friendId = friendID;
    this.chatMessage = !this.chatMessage;
  }
  ngOnInit(): void {
    this.user = this.authService.getUser();
  }
}
