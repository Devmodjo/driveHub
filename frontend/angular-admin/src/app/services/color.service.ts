import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ColorService {
  constructor() { }

  public stringToRGB(str: string): string {
    if (str)
      return this.intToRGB(this.hashCode(str.replace(' ', '').trim()));
    return 'crimson';
  }

  public intToRGB(i: number) {
    const c = (i & 0x00FFFFFF)
      .toString(16)
      .toUpperCase();
    return '#' + '00000'.substring(0, 6 - c.length) + c;
  }

  public hashCode(str: string) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return hash;
  }
}
