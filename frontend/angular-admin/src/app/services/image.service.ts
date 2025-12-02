import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImageService {

  constructor(public http: HttpClient) { }

  public defaultProfilImage = 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBwgHBgkIBwgKCgkLDRYPDQwMDRsUFRAWIB0iIiAdHx8kKDQsJCYxJx8fLT0tMTU3Ojo6Iys/RD84QzQ5OjcBCgoKDQwNGg8PGjclHyU3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3N//AABEIAJQAoAMBIgACEQEDEQH/xAAbAAEAAwEBAQEAAAAAAAAAAAAABAUGAwECB//EADgQAAICAQIDBAcGBQUAAAAAAAABAgMRBAUSITEiQVFxBhNSYYGRwSMzYnKh0RQyQrHhNUNEVJL/xAAUAQEAAAAAAAAAAAAAAAAAAAAA/8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/aAAwDAQACEQMRAD8A/UQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADlqb4aat2WPku7x9wHLW66vSLDXHY1ygvqypt3TVTfYkq14RX1ZGvsd107JZzJ55nPIHd63VPm9RZ8x/Gar/sW/8Ao4MZAlR3DVx/3m/PDLLRbnC5qu9KuWeTzyf7FGANaCt2fVyuhKm2WZwWYt9WiyAAAAAAAAAAAAAABT79J8VMefDhsuCo39ctO/zL+wH3sezx1sHqNU5KrOIxi8cX+C2n6P7fLpCcX4xmyTtFfq9r0scYbrUn5vn9SWBUx9HtAn2vXS/NP9jtHZNuj/x0/OTLAAVtmxbfNNKlw98JNYM7u+3S266MVLirnzhJ9fJm0M96Xfy6Xzl9AKXQT4NbS0+suH4M0pmdvjxa6lfjyabu94AAAAAAAAAAAAAAK3eoO1aaK/qs4V8cFke2aP8AiK6bU+1C+Esd2M8wLSMVCKiukVg9AAAAAUfpZW3pKbF0jPD+KLw+Lqq765VXwU65LnFgYzZ48Wuh+GLZoCJsG2SruuuuXYTlXDPWWH1J1qUbZRXRPAHwAAAAAAAAAAAAAEzQPszXgyGSNFNQlJSaWUBOAQAAAAAAPJyUIuXcl0Kptybb7ybrLY8DhF5b6+RCAAAAAAAAAAAAAAAAAstPLipi+/GGdCHorMN1vv5omAAAAOOrlw6eWG8vkdspc28Igaq12zSgnwx/UDgAAAAAAAAAAAAAAAAAAPU8c11J1dzSXF+hBiufPp1ZLoXHTCXe48wO/rYe/wCR47V3LJ88A4QOVjlZ1eF4CqvDbfhg6tI533V6emVtr4YRXMCD6yDutqT7db5xf9z6M9LcbJbi9XKOMvDivZ8C+qnC2uNlbzGXRgfYAAAAAAAAAAABtLLbSS7wB5KSjFyk0klzb7iBqt1pqzGr7SXj/SviVGp1V+pl9pLK7oLoBqNNON+mttr5xb4Yy8cdSbpPuF5tEbSRqooq0fF21Wsol0rgqXcB0wfLeD4d9fEo8az5nvUB9DKb1uL1l3q6m/UVvl+J+P7E/wBINx4E9JQ+3L7xp9F4Ge6ACVo9dbpHiOJQb5xZFAGi0u4Uajsp8E/Zl3+TJZkidpNzu0+ITzZX7+q8mBfg4abV06lfZT598Xya+B3AAAAfF1sKa5WWS4Yo+yp323CqqXe+J/QBdvMelFTb9qfT5FdqNVdqH9rNteyuSOK6AAd9FD1msog+jsWfLJwJ2xx4tyq/Cm/0A0cKu3KbXak8tkhtuOGzxcgBxsqUu4i63cJaHTtPtWSWK33p+JPnJQhKUmlFLLb7jI7hqnq9TKx54VygvBAR5ScpOUm228tvvPAAAAAAAAuTTXJrpgn6Xdb6lw2/aQXj1+ZAAGl0usp1X3Uu17L6kgytVsqboWx6xeUamDU4RlH+VrKA9XVGc3Wcpa+3ifTCXyQAEUAACx9H/wDUo/kkeADUg8AFV6R2zhpIQi8RsliXvSRnEAAAAAAAAAAAADODRbS3Lb6nJ5ayvkwAP//Z';

  public uploadFile(file: File): Observable<string> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post(`${environment.basePath}/api/files/upload`, formData, {
      responseType: 'text'
    });
  }


  public downloadFile(id: string | undefined): Observable<Blob> {
    const url = `${environment.basePath}/api/files/download/${id}`;
    return this.http.get(url, { responseType: 'blob' });
  }

}