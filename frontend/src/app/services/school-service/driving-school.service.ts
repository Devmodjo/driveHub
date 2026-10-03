import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiResponse } from '../../interfaces/ApiResponse';
import { Observable } from 'rxjs';
import { BASE_URL, SCHOOL_URL } from '../../utils/UTILS';
import { ActiveDrivingSchool } from '../../interfaces/ActiveDrivingSchool';
import { RegistryStats } from '../../interfaces/RegistryStats';
import { SchoolRegistryDetail } from '../../interfaces/SchoolRegistryDetail';
import { PagedResponse } from '../../interfaces/PagedResponse';
import { PendingSchoolRequest } from '../../interfaces/PendingSchoolRequest';
import { HttpParams } from '@angular/common/http';


@Injectable({
  providedIn: 'root',
})  
export class DrivingSchoolService {

  private http = inject(HttpClient);
  
  
  /**
   * active une auto école et son Moniteur
   * @param registryId id du registry à activer
   * @returns Observable<ApiResponse>
   */
  approveDrivingSchooleRequest(registryId:string) : Observable<ApiResponse> {
    return this.http.patch<ApiResponse>(`${BASE_URL}registries/${registryId}/approve`, null)
  }

  activeDrivingSchool() : Observable<ActiveDrivingSchool[]> {
    return this.http.get<ActiveDrivingSchool[]>(`${SCHOOL_URL}public/all`);
  }

  /** Demandes de création d'auto-école en attente (DrivingSchoolPendingRequestDTO côté backend). */
  pendingSchoolRequest() : Observable<PendingSchoolRequest[]> {
    return this.http.get<PendingSchoolRequest[]>(`${BASE_URL}registries/pending`);
  }

  approveRegistry(registryId:string) : Observable<ApiResponse> {
    return this.http.patch<ApiResponse>(`${BASE_URL}registries/${registryId}/approve`, null);
  }

  suspendRegistry(registryId:string): Observable<ApiResponse> {
    return this.http.patch<ApiResponse>(`${BASE_URL}registries/${registryId}/suspend`, null);
  }

  rejectRegistry(registryId:string) : Observable<ApiResponse> {
    return this.http.patch<ApiResponse>(`${BASE_URL}registries/${registryId}/reject`, null);
  }

  getSchoolRegistryStats() : Observable<RegistryStats> {
    return this.http.get<RegistryStats>(`${BASE_URL}registries/stats`);
  }

  getSchoolRegistryDetail(registryId:string) : Observable<SchoolRegistryDetail> {
    return this.http.get<SchoolRegistryDetail>(`${BASE_URL}registries/${registryId}`);
  }

  deleteRegistry(registryId:string) : Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${BASE_URL}registries/${registryId}`);
  }

  getAllRegistry(
    page: number = 0,
    size: number = 10,
    status?: string,
  ) : Observable<PagedResponse<SchoolRegistryDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) params = params.set('status', status);

    return this.http.get<PagedResponse<SchoolRegistryDetail>>(`${BASE_URL}registries`, { params });
  }

}
