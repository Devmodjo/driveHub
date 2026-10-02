import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import {
  ApiMessage, AuthResponse, CurrentUser, ProfileStatus, RegisterRequest, UserRole,
} from '../../interfaces/drivehub.models';
import { API_URL } from '../../utils/UTILS';

/** Contenu utile du JWT émis par le backend (JwtService.generateToken). */
export interface SessionClaims {
  email: string;
  role: UserRole;
  profileStatus: ProfileStatus;
  /** Schéma de l'auto-école de l'utilisateur ; absent tant qu'il n'appartient à aucune auto-école. */
  tenant: string | null;
  expiresAt: number;
}

/**
 * Session des utilisateurs de l'espace auto-école (moniteurs et élèves).
 *
 * À ne pas confondre avec AuthService, qui gère les administrateurs de la plateforme (back-office)
 * avec un autre jeton ("drivehub_token"). Les deux sessions sont indépendantes.
 *
 * Le jeton contient le "tenant" (schéma PostgreSQL de l'auto-école). L'intercepteur HTTP le recopie
 * dans l'en-tête X-Tenant-ID, comme l'attend TenantResolutionFilter côté backend.
 * Quand l'utilisateur rejoint une auto-école (ou que la sienne est approuvée), refresh() demande
 * un nouveau jeton qui contient le tenant.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly http = inject(HttpClient);
  private readonly TOKEN_KEY = 'drivehub_user_token';

  private readonly token = signal<string | null>(this.readToken());

  readonly claims = computed<SessionClaims | null>(() => decode(this.token()));
  readonly isLoggedIn = computed(() => {
    const claims = this.claims();
    return !!claims && claims.expiresAt > Date.now();
  });
  readonly role = computed(() => this.claims()?.role ?? null);
  readonly tenant = computed(() => this.claims()?.tenant ?? null);
  readonly isMonitor = computed(() => this.role() === 'MONITOR');

  getToken(): string | null {
    return this.isLoggedIn() ? this.token() : null;
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}auth/login`, { email, password })
      .pipe(tap((res) => this.store(res.token)));
  }

  /** Nouveau jeton avec le tenant à jour (après une approbation). */
  refresh(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}auth/refresh-token`, {})
      .pipe(tap((res) => this.store(res.token)));
  }

  register(role: UserRole, request: RegisterRequest): Observable<ApiMessage> {
    const path = role === 'MONITOR' ? 'monitor' : 'student';
    return this.http.post<ApiMessage>(`${API_URL}auth/register/${path}`, request);
  }

  me(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${API_URL}auth/me`);
  }

  verifyEmail(token: string): Observable<ApiMessage> {
    return this.http.get<ApiMessage>(`${API_URL}auth/verify-email`, { params: { token } });
  }

  resendVerification(email: string): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_URL}auth/resend-verification`, { email });
  }

  forgotPassword(email: string): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_URL}auth/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_URL}auth/reset-password`, { token, newPassword });
  }

  /**
   * Déconnexion : le backend invalide le jeton (POST /api/auth/logout), puis on l'efface du navigateur.
   * Le jeton est envoyé explicitement car il est effacé localement avant la fin de la requête.
   */
  logout(): void {
    const token = this.token();
    this.clear();
    if (token) {
      this.http.post(`${API_URL}auth/logout`, {}, { headers: { Authorization: `Bearer ${token}` } })
        .subscribe({ error: () => undefined });   // jeton déjà expiré : rien à faire
    }
  }

  /** Efface la session locale sans appeler le backend (jeton expiré ou refusé). */
  clear(): void {
    this.token.set(null);
    try {
      localStorage.removeItem(this.TOKEN_KEY);
    } catch {
      // ignore
    }
  }

  private store(token: string): void {
    this.token.set(token);
    try {
      localStorage.setItem(this.TOKEN_KEY, token);
    } catch {
      // stockage indisponible : la session dure le temps de l'onglet
    }
  }

  private readToken(): string | null {
    try {
      return localStorage.getItem(this.TOKEN_KEY);
    } catch {
      return null;
    }
  }
}

/** Lit la partie "payload" du JWT (sans vérifier la signature : c'est le rôle du backend). */
function decode(token: string | null): SessionClaims | null {
  if (!token) return null;
  try {
    const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(part).split('').map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''),
    );
    const payload = JSON.parse(json);
    if (payload.tokenType && payload.tokenType !== 'USER') return null;
    return {
      email: payload.sub,
      role: payload.role,
      profileStatus: payload.profileStatus,
      tenant: payload.tenant ?? null,
      expiresAt: (payload.exp ?? 0) * 1000,
    };
  } catch {
    return null;
  }
}
