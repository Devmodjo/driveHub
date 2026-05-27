// =============================================
// Imports Angular : outils de base du composant
// =============================================
import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';     // Pour formater les dates dans le HTML
import { RouterLink } from '@angular/router';   // Pour les liens de navigation

// =============================================
// Imports : services (communication avec l'API)
// =============================================
import { AdminService } from '../../../services/admin-service/admin.service';
import { DrivingSchoolService } from '../../../services/school-service/driving-school.service';

// =============================================
// Imports : interfaces (modèles de données)
// =============================================
import { AdminStats } from '../../../interfaces/AdminStats';
import { ActiveDrivingSchool } from '../../../interfaces/ActiveDrivingSchool';
import { AdminProfile } from '../../../interfaces/AdminProfile';

// =============================================
// Import : sous-composant KPI
// =============================================
import { DashboardKpiComponent } from '../../../components/dashboard-component/dashdoard-kpi/dashboard.kpi.component';

// =============================================
// Déclaration du composant Angular
// =============================================
@Component({
  selector: 'app-overview',                        // Balise HTML qui représente ce composant
  imports: [DashboardKpiComponent, RouterLink, DatePipe],
  providers: [AdminService, DrivingSchoolService], // Services disponibles uniquement ici
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css',
})
export class OverviewComponent implements OnInit {

  private adminService = inject(AdminService);
  private drivingSchoolService = inject(DrivingSchoolService);


  adminstats = signal<AdminStats | null>(null);          
  countActiveSchool = signal<number>(0);                 
  activeSchools = signal<ActiveDrivingSchool[]>([]);     
  pendingSchools = signal<ActiveDrivingSchool[]>([]);    
  countPendingSchoolRequest = signal<number>(0);         
  pendingAdmins = signal<AdminProfile[]>([]);            
  actionMessage = signal<{ text: string; type: 'success' | 'error' } | null>(null); // Message toast


  ngOnInit(): void {
    this.loadData();
  }

  // Charge toutes les données depuis l'API
  // Chaque appel est indépendant et met à jour son signal correspondant
  loadData() {

    // Récupère les statistiques globales des admins
    this.adminService.getAdminStats().subscribe({
      next: (res) => this.adminstats.set(res),
      error: (err) => console.error('Stats error', err),
    });

    // Récupère les admins en attente d'activation
    this.adminService.getAdminsPendingRequest().subscribe({
      next: (res) => this.pendingAdmins.set(res),
      error: (err) => console.error('Pending admins error', err),
    });

    // Récupère les auto-écoles en attente d'approbation
    this.drivingSchoolService.pendingSchoolRequest().subscribe({
      next: (res) => {
        this.pendingSchools.set(res);
        this.countPendingSchoolRequest.set(res.length); // Met aussi à jour le compteur
      },
      error: (err) => console.error('Pending schools error', err),
    });

    // Récupère les auto-écoles actives
    this.drivingSchoolService.activeDrivingSchool().subscribe({
      next: (res) => {
        this.activeSchools.set(res);
        this.countActiveSchool.set(res.length); // Met aussi à jour le compteur
      },
      error: (err) => console.error('Active schools error', err),
    });
  }

  // Active un admin en attente (via son identifiant)
  activateAdmin(id: string) {
    this.adminService.activateAdmin(id).subscribe({
      next: () => {
        this.showMessage('Admin activé avec succès', 'success');
        this.loadData(); // Recharge les données pour refléter le changement
      },
      error: () => this.showMessage('Erreur lors de l\'activation', 'error'),
    });
  }

  // Approuve la demande d'une auto-école (via son identifiant)
  approveSchool(id: string) {
    this.drivingSchoolService.approveDrivingSchooleRequest(id).subscribe({
      next: () => {
        this.showMessage('Auto-école approuvée', 'success');
        this.loadData(); // Recharge les données pour refléter le changement
      },
      error: () => this.showMessage('Erreur lors de l\'approbation', 'error'),
    });
  }

  // =============================================
  // Génère les initiales d'un nom (ex: "Jean Dupont" → "JD")
  // Utilisé pour afficher un avatar textuel dans la liste des admins
  // =============================================
  getInitials(name: string): string {
    return name
      .split(' ')          // Sépare le nom en mots
      .map(n => n[0])      // Prend la première lettre de chaque mot
      .join('')            // Recolle les lettres ensemble
      .toUpperCase()       // Met en majuscules
      .slice(0, 2);        // Garde seulement les 2 premières initiales
  }

  // Affiche un message toast (succès ou erreur) pendant 3,5 secondes
  private showMessage(text: string, type: 'success' | 'error') {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500); // Efface le message après 3,5s
  }
}
