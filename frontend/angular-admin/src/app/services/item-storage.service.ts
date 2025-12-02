import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NavigationService } from './navigation.service';

export const ITEM_TO_UPDATE_KEY: string = 'ITEM_TO_UPDATE_KEY';


@Injectable({
  providedIn: 'root'
})
export class ItemStorageService {

  private http = inject(HttpClient);
  public navigationService = inject(NavigationService);

  constructor() { }

  public setItemToUpdateORView(): void {
    const item = localStorage.getItem(ITEM_TO_UPDATE_KEY);
    if (item) {
      return JSON.parse(item);
    }
  }

  public getItemToUpdate(): void {
    const item = localStorage.getItem(ITEM_TO_UPDATE_KEY);
    if (item) {
      return JSON.parse(item);
    }
  }

  public deleteItemToUpdate(): void {
    localStorage.removeItem(ITEM_TO_UPDATE_KEY);
  }

}
