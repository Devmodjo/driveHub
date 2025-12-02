import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'fcfacurrency'
})
export class FcfacurrencyPipe implements PipeTransform {
  transform(value: number | string): string {
    const amount = typeof value === 'string' ? parseFloat(value) : value;

    if (isNaN(amount)) {
      return 'Montant invalide';
    }

    return `${amount.toLocaleString()} FCFA`;
  }
}
