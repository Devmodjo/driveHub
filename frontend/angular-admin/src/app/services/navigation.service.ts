import { inject, Injectable } from '@angular/core';
import { NavigationExtras, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {

  private router = inject(Router);

  constructor() { }

  goTo(url: string) {
    this.router.navigate([url]);
  }

  public back(): void {
    window.history.back();
  }

  goToWithId(url: string, id: number, uid?: number) {
    const extra: NavigationExtras = {
      queryParams: {
        id: id,
        uid: uid ? uid : null
      }
    };
    this.router.navigate([url], extra);
  }

  goToWithData(url: string, id: number, uid?: number, data?: Record<string, unknown>): void {
    const extra: NavigationExtras = {
      queryParams: {
        id: id,
        uid: uid ?? null,
        data: data ? JSON.stringify(data) : null,
      },
    };
    this.router.navigate([url], extra);
  }

  goToWithParams(url: string, params: string | Record<string, unknown>, userId: number): void {
    const extra: NavigationExtras = {
      queryParams: {
        param: typeof params === 'string' ? params : JSON.stringify(params),
        uid: userId,
      },
    };
    this.router.navigate([url], extra);
  }
}
