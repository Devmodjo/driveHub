import { Component, inject, signal, OnInit } from '@angular/core';
import { AdminService } from '../../../services/admin-service/admin.service';
import { AdminStats } from '../../../interfaces/AdminStats';
import { DashboardKpiComponent } from '../../../components/dashboard-component/dashdoard-kpi/dashboard.kpi.component';
import { DrivingSchoolService } from '../../../services/school-service/driving-school.service';
import { ActiveDrivingSchool } from '../../../interfaces/ActiveDrivingSchool';
import { AdminProfile } from '../../../interfaces/AdminProfile';

/**
 * Page "Vue d'ensemble" du dashboard.
 * Route : /dashboard/overview
 * Contiendra les KPI cards une fois les données disponibles.
 */
@Component({
  selector: 'app-overview',
  imports: [DashboardKpiComponent],
  providers : [AdminService, DrivingSchoolService],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css',
})
export class OverviewComponent implements OnInit {

  private adminService = inject(AdminService);
  private drivingSchoolService = inject(DrivingSchoolService);
  
  // Rendu public pour y accéder dans le template HTML
  adminstats = signal<AdminStats | undefined | null>(null);


  countActiveSchool = signal<number>(0);
  activeSchools = signal<ActiveDrivingSchool[] | null>([]);

  pendingSchool = signal<ActiveDrivingSchool[] | null>([]);
  countPendingSchoolRequest = signal<number>(0);

  ngOnInit(): void {
   
    this.loadAdminStats();
   
    this.loadActiveAutoSchool();

    this.loadPendingSchoolRequest();
  }

  loadAdminStats() {
    this.adminService.getAdminStats().subscribe({
      next : (response) => {
        this.adminstats.set(response);
      },
      error : (error) => {
        console.error("Erreur de chargement des stats", error);
      }
    });
  }

  loadPendingSchoolRequest() {
    this.drivingSchoolService.pendingSchoolRequest().subscribe({
      next : (response) => {
        this.pendingSchool.set(response);
        this.countPendingSchoolRequest.set(response.length);
      },
      error : (error) =>{
        console.error("Erreur de chargement des Admins", error);
      }
    })
  }

  loadActiveAutoSchool() {
    this.drivingSchoolService.activeDrivingSchool().subscribe({
      next : (response) =>{
        this.activeSchools.set(response);
        this.countActiveSchool.set(response.length);
      },
      error : (error) =>{
        console.error("Erreur de chargement des auto-écoles", error);
      }
    });
  }
}
