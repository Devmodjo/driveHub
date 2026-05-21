import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiResponse } from '../../interfaces/ApiResponse';
import { Observable } from 'rxjs';
import { BASE_URL, SCHOOL_URL } from '../../utils/UTILS';
import { ActiveDrivingSchool } from '../../interfaces/ActiveDrivingSchool';


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

  pendingSchoolRequest() : Observable<ActiveDrivingSchool[]> {
    return this.http.get<ActiveDrivingSchool[]>(`${BASE_URL}registries/pending`);
  }
}
