import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AdminProfile } from '../../interfaces/AdminProfile';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../interfaces/ApiResponse';
import { PagedResponse } from '../../interfaces/PagedResponse';
import { BASE_URL } from '../../utils/UTILS';
import { AdminUpdate } from '../../interfaces/AdminUpdate';
import { AdminStats } from '../../interfaces/AdminStats';
import { ChangePassword } from '../../interfaces/ChangePassword';

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
   * met à jour les information de l'utilisateur connecté
   * @param adminUpdate les information à mettre à jour
   * @returns Observable<AdminProfile>
   */
  updateCurrentAdmin(adminUpdate : AdminUpdate) : Observable<AdminProfile> {
    return this.http.patch<AdminProfile>(`${BASE_URL}admin/me`, adminUpdate);
  }

  /**
   * change le mot de passe de l'utilisateur connecté
   * @param password les information à mettre à jour
   * @returns Observable<ApiResponse>
   */
  changePassword(password : ChangePassword) : Observable<ApiResponse>  {
    return this.http.patch<ApiResponse>(`${BASE_URL}admin/me/password`, password);
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
   * Récupère la liste paginée de tous les admins.
   * Accessible uniquement aux ROOT.
   *
   * @param page   - Numéro de page (commence à 0). Défaut : 0
   * @param size   - Nombre d'éléments par page. Défaut : 10
   * @param status - Filtre optionnel par statut (ACTIVE, PENDING, INACTIVE)
   * @param role   - Filtre optionnel par rôle (REVIEWER, ROOT, SUPER_ADMIN)
   * @returns Observable<PagedResponse<AdminProfile>>
   */
  getAllAdmins(
    page: number = 0,
    size: number = 10,
    status?: string,
    role?: string
  ): Observable<PagedResponse<AdminProfile>> {
    // On construit les paramètres de requête dynamiquement
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    // Les filtres status et role ne sont ajoutés que s'ils sont définis
    if (status) params = params.set('status', status);
    if (role)   params = params.set('role', role);

    return this.http.get<PagedResponse<AdminProfile>>(`${BASE_URL}admin`, { params });
  }

  /**
   * Affiche la liste des admins en attente de validation.
   * @returns Observable<AdminProfile[]>
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
  
  /**
   * affiche les statistique des admins
   * @returns Observable<AdminStats>
   */
  getAdminStats() : Observable<AdminStats> {
    return this.http.get<AdminStats>(`${BASE_URL}admin/stats`);
  }

  /**
   * supprime un admin
   * @param adminId id de l'admin à supprimer
   * @returns Observable<ApiResponse>
   */
  deleteAdmin(adminId: string) : Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${BASE_URL}admin/${adminId}`);
  }

  
  
}
