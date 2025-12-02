import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { LoginData } from '../additional-models/LoginData';
import { UserWrapper } from '../additional-models/users-wrapper';
import { NavigationService } from './navigation.service';
import { AuthenticationResponse } from '../generated/model/authenticationResponse';
import { User } from '../generated/model/user';

export const USER_KEY: string = 'USER_KEY';
export const ACCESS_TOKEN_KEY: string = 'ACCESS_TOKEN_KEY';
export const REFRESH_TOKEN_KEY: string = 'REFRESH_TOKEN_KEY';
export const TOKEN_EXPIRED_AT_KEY: string = 'TOKEN_EXPIRED_AT_KEY';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private http = inject(HttpClient);
  public navigationService = inject(NavigationService);

  constructor() { }


  login(loginData: LoginData): Observable<UserWrapper> {
    return this.http.post<UserWrapper>(`${environment.basePath}/api/v1/auth/authenticate`, loginData);
  }

  register(user: User): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${environment.basePath}/users/createOrUpdateUser`, user);
  }

  public storeUser(user: User): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): User | null {
    const userString = localStorage.getItem(USER_KEY);
    if (userString)
      return JSON.parse(userString);
    return null;
  }

  public deleteUser(): void {
    localStorage.removeItem(USER_KEY);
  }

  public storeAccessToken(token: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  }

  public getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  public storeRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  }

  public getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  public storeTokenExpiresAt(tokenExpiresAt: string): void {
    localStorage.setItem(TOKEN_EXPIRED_AT_KEY, tokenExpiresAt);
  }

  public deleteTokenExpiresAt(): void {
    localStorage.removeItem(TOKEN_EXPIRED_AT_KEY);
  }

  public getTokenExpiresAt(): string | null {
    return localStorage.getItem(TOKEN_EXPIRED_AT_KEY);
  }

  public deleteAccessToken(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }

  public deleteRefreshToken(): void {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }

  public localLogin(user: User, accessToken: string, expiresAt: string): void {
    this.storeUser(user);
    this.storeAccessToken(accessToken);
    this.storeTokenExpiresAt(expiresAt);
  }

  public logout(): void {
    this.deleteUser();
    this.deleteAccessToken();
    this.deleteRefreshToken();
    this.deleteTokenExpiresAt();
    this.navigationService.goTo('login');
  }

  public isLoggedIn(): boolean {
    const user: User = this.getUser()!;
    const token: string = this.getAccessToken()!;
    const tokenExpiresAt: string = this.getTokenExpiresAt()!;
    const now = (new Date()).getTime();
    if (user && token && tokenExpiresAt) {
      const expires_at = (new Date(tokenExpiresAt)).getTime();
      return (now < expires_at);
    } else {
      return false;
    }
  }

  public hasAnyRole(roles: string[]): boolean {
    if (this.isLoggedIn()) {
      const userRoles: string[] | undefined = this.getUser()?.roles;
      if (!userRoles)
        return false;
      for (let index = 0; index < userRoles.length; index++) {
        if (roles.includes(userRoles[index].trim())) {
          return true;
        }
      }
      return false;
    } else {
      return false;
    }
  }

  // public hasAnyPermission(permissions: string[]): boolean {
  //   if (this.isLoggedIn()) {
  //     if (this.getUser()?.roles?.includes(ROLES.ADMIN_ROLE.toString())) {
  //       return true;
  //     }
  //     const userPermissions: string[] = this.getUser()!.permissions!;
  //     for (let index = 0; index < userPermissions.length; index++) {
  //       if (permissions.includes(userPermissions[index].trim())) {
  //         return true;
  //       }
  //     }
  //     return false;
  //   } else {
  //     return false;
  //   }
  // }


}
