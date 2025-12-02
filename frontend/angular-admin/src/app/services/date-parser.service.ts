import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class DateParserService {
  constructor() { }

  parseToLocalFr(date: Date): string {
    const options: Intl.DateTimeFormatOptions = {
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: 'numeric'
    };
    return date.toLocaleDateString('fr-FR', options);
  }

  public getFormattedDate(dateString: string): string | null {
    return new DatePipe('en-US').transform(dateString, 'dd/MM/yyyy');
  }

  public formDateFormatter(date: string): string {
    const dataSplit = date.split('/');

    if (dataSplit[2].split(" ").length > 1) {
      dataSplit[2] = dataSplit[2].split(" ")[0];
    }
    return new Date(Number.parseInt(dataSplit[2]), Number.parseInt(dataSplit[1]) - 1, Number.parseInt(dataSplit[0]) + 1).toISOString().substring(0, 10);
  }

  public getDateAsString(dateString: Date): string | null {
    return new DatePipe('en-US').transform(dateString, 'dd/MM/yyyy');
  }

  public convertStringToDate(date: string): Date {
    const dataSplit = date.split('/');
    const day = Number.parseInt(dataSplit[0]);
    const month = Number.parseInt(dataSplit[1]);
    const year = Number.parseInt(dataSplit[2]);
    return new Date(year, month - 1, day);
  }

  public dateFormatter(date: string): Date {
    const dataSplit = date.split('/');
    if (dataSplit[2].split(" ").length > 1) {
      dataSplit[2] = dataSplit[2].split(" ")[0];
    }
    return new Date(Number.parseInt(dataSplit[2]), Number.parseInt(dataSplit[1]) - 1, Number.parseInt(dataSplit[0]) + 1);
  }

  dateFormatterStringToDate(dateString?: string): string {
    if (!dateString || typeof dateString !== 'string') {
      return 'N/A';
    }

    try {
      const date = new Date(dateString);

      if (isNaN(date.getTime())) {
        return 'N/A';
      }
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const year = date.getFullYear();

      return `${day}/${month}/${year}`;
    } catch (error) {
      return 'N/A';
    }
  }

  public transformStringDate(value: string): string {
    const parts = value.split('/');
    if (parts.length === 3) {
      const day = parts[0];
      const month = parts[1];
      const year = parts[2];
      if (parseInt(day) && parseInt(month) && parseInt(year)) {
        return `${year}-${month}-${day}`;
      }
    }
    return value;
  }
}
