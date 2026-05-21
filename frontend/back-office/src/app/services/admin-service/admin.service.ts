import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AdminProfile } from '../../interfaces/AdminProfile';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../interfaces/ApiResponse';
import { BASE_URL } from '../../utils/UTILS';
import { AdminUpdate } from '../../interfaces/AdminUpdate';
import { AdminStats } from '../../interfaces/AdminStats';

@Injectable({
  providedIn: 'root',
})
export class AdminService {


  private http = inject(HttpClient);
  
  /**
   * Récupère les informations de l'utilisateur connecté.
   * @returns Observable<AdminProfile>
   */
  getCurrentAdmin() : Observable<AdminProfile> {
    return this.http.get<AdminProfile>(`${BASE_URL}admin/me`);
  }

  /**
   * Afficher les information d'un
   * admin specifique
   */
  getAdminDetails(adminId : string) : Observable<AdminProfile> {
    return this.http.get<AdminProfile>(`${BASE_URL}admin/${adminId}`);
  }

  /**
   * mettre à jour les information d'un admin
   */
  updteAdmin(adminId : string) : Observable<AdminProfile>{
    return this.http.patch<AdminProfile>(`${BASE_URL}admin/${adminId}`,null)
  }

  /**
   * affiche la liste des admins en attente de validation
   * @returns Observable<AdminProfile>
   */
  getAdminsPendingRequest() : Observable<AdminProfile[]> {
    return this.http.get<AdminProfile[]>(`${BASE_URL}admin/pending`);
  }
  /**
   * active un admin
   * @param adminId id de l'admin à activer
   * @returns Observable<ApiResponse>
   */
  activateAdmin(adminId : string) : Observable<ApiResponse> {
    return this.http.patch<ApiResponse>(`${BASE_URL}admin/${adminId}/activate`, null)
  }

  /**
   * déactive un admin
   * @param adminId id de l'admin à activer
   * @returns Observable<ApiResponse>
   */
  disableAdmin(adminId: string) : Observable<ApiResponse> {
    return this.http.patch<ApiResponse>(`${BASE_URL}admin/${adminId}/activate`, null);
  }
  
  getAdminStats() : Observable<AdminStats> {
    return this.http.get<AdminStats>(`${BASE_URL}admin/stats`);

  }

  
  
}
