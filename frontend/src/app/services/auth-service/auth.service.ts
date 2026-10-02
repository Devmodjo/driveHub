import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import UserLoginCredentials from '../../interfaces/UserLoginCredentials';
import { BASE_URL } from '../../utils/UTILS';
import { Observable, Observer, tap } from 'rxjs';
import UserRegisterModel from '../../interfaces/UserRegisterModel';
import { ApiResponse } from '../../interfaces/ApiResponse';
import { LoginResponse } from '../../interfaces/LoginResponse';
import { AdminProfile } from '../../interfaces/AdminProfile';

/**
 * Service AuthService : gère la logique d'authentification
 * entre les composants Register et Login.
 */
@Injectable({
  providedIn: 'root', // portée globale (toute l'application)
})
export class AuthService {


  private http = inject(HttpClient);
  private readonly TOKEN_KEY = "drivehub_token"

  register(user: UserRegisterModel):Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${BASE_URL}admin/register`, user);
  }

  login(credentials: UserLoginCredentials): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${BASE_URL}admin/login`, credentials);
  }

  /**
   * Encode et sauvegarde le token dans le localStorage.
   * btoa() convertit la chaîne en Base64 (encodage basique, pas du chiffrement).
   * @param token Le token brut reçu de l'API
  */  
  saveToken(token: string): void {
    const encodedToken = btoa(token)
    localStorage.setItem(this.TOKEN_KEY, encodedToken);
  }

  /**
 * Récupère et décode le token depuis le localStorage.
 * atob() est l'inverse de btoa() — il décode le Base64.
 * Retourne null si aucun token n'existe.
 */
  getToken(): string | null {
    const encodedToken = localStorage.getItem(this.TOKEN_KEY);
    if (!encodedToken) return null;
    return atob(encodedToken); // décode le Base64
  }

  /**
   * Récupère les informations de l'utilisateur connecté.
   * @returns Observable<AdminProfile>
   */
  getCurrentAdmin() : Observable<AdminProfile> {
    return this.http.get<AdminProfile>(`${BASE_URL}admin/me`);
  }


  /**
   * Vérifie si l'utilisateur est connecté.
   * !! convertit la valeur en boolean (null → false, string → true)
   */
  isLoggedIn(): boolean {
    return !!this.getToken();
  }
  /**
   * Déconnecte l'utilisateur en supprimant le token du localStorage.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  
  
}
