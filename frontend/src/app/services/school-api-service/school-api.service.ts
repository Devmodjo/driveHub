import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage, Course, CourseRequest, Exam, ExamInscription, ExamRequest, InscriptionStatus, Monitor,
  MyJoinRequest, MySchoolRegistry, Page, Payment, PaymentRequest, PaymentSummary, PendingJoinRequest,
  PublicSchool, Reservation, ReservationRequest, SchoolRequest, Student, StudentUpdate, UserRole, Vehicle,
  VehicleRequest,
} from '../../interfaces/drivehub.models';
import { API_URL } from '../../utils/UTILS';

/**
 * Appels HTTP de l'espace auto-école / élève.
 *
 * - Routes du schéma public (/api/driving-schools, /api/join-school) : pas besoin de tenant.
 * - Routes métier (/api/students, /api/vehicles, ...) : l'intercepteur ajoute X-Tenant-ID
 *   à partir du jeton, le backend lit alors les tables du schéma de l'auto-école.
 */
@Injectable({ providedIn: 'root' })
export class SchoolApiService {
  private readonly http = inject(HttpClient);

  // ─── Schéma public ────────────────────────────────────────────────
  publicSchools(): Observable<PublicSchool[]> {
    return this.http.get<PublicSchool[]>(`${API_URL}driving-schools/public/all`);
  }

  requestSchool(request: SchoolRequest): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_URL}driving-schools/request`, request);
  }

  /** Demande de création d'auto-école du moniteur connecté (null si aucune). */
  mySchool(): Observable<MySchoolRegistry | null> {
    return this.http.get<MySchoolRegistry | null>(`${API_URL}driving-schools/me`);
  }

  requestJoin(drivingSchoolId: string, role: UserRole): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_URL}join-school/public`, { drivingSchoolId, role });
  }

  myJoinRequests(): Observable<MyJoinRequest[]> {
    return this.http.get<MyJoinRequest[]>(`${API_URL}join-school/me`);
  }

  pendingJoinRequests(page = 0, size = 20): Observable<Page<PendingJoinRequest>> {
    return this.http.get<Page<PendingJoinRequest>>(`${API_URL}join-school/admin/pending`, { params: paging(page, size) });
  }

  approveJoin(id: string): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_URL}join-school/admin/${id}/approve`, {});
  }

  rejectJoin(id: string): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${API_URL}join-school/admin/${id}/reject`, {});
  }

  // ─── Élèves et moniteurs ──────────────────────────────────────────
  students(page = 0, size = 50): Observable<Page<Student>> {
    return this.http.get<Page<Student>>(`${API_URL}students`, { params: paging(page, size) });
  }

  myStudentProfile(): Observable<Student> {
    return this.http.get<Student>(`${API_URL}students/me`);
  }

  updateStudent(id: string, request: StudentUpdate): Observable<Student> {
    return this.http.put<Student>(`${API_URL}students/${id}`, request);
  }

  deleteStudent(id: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${API_URL}students/${id}`);
  }

  monitors(): Observable<Monitor[]> {
    return this.http.get<Monitor[]>(`${API_URL}monitors`);
  }

  // ─── Véhicules ────────────────────────────────────────────────────
  vehicles(page = 0, size = 50): Observable<Page<Vehicle>> {
    return this.http.get<Page<Vehicle>>(`${API_URL}vehicles`, { params: paging(page, size) });
  }

  createVehicle(request: VehicleRequest): Observable<Vehicle> {
    return this.http.post<Vehicle>(`${API_URL}vehicles`, request);
  }

  updateVehicle(id: string, request: VehicleRequest): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${API_URL}vehicles/${id}`, request);
  }

  deleteVehicle(id: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${API_URL}vehicles/${id}`);
  }

  // ─── Cours ────────────────────────────────────────────────────────
  courses(page = 0, size = 50): Observable<Page<Course>> {
    return this.http.get<Page<Course>>(`${API_URL}courses`, { params: paging(page, size) });
  }

  createCourse(request: CourseRequest): Observable<Course> {
    return this.http.post<Course>(`${API_URL}courses`, request);
  }

  updateCourse(id: string, request: CourseRequest): Observable<Course> {
    return this.http.put<Course>(`${API_URL}courses/${id}`, request);
  }

  deleteCourse(id: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${API_URL}courses/${id}`);
  }

  // ─── Examens ──────────────────────────────────────────────────────
  exams(page = 0, size = 50): Observable<Page<Exam>> {
    return this.http.get<Page<Exam>>(`${API_URL}exams`, { params: paging(page, size) });
  }

  createExam(request: ExamRequest): Observable<Exam> {
    return this.http.post<Exam>(`${API_URL}exams`, request);
  }

  deleteExam(id: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${API_URL}exams/${id}`);
  }

  examInscriptions(examId: string): Observable<ExamInscription[]> {
    return this.http.get<ExamInscription[]>(`${API_URL}exams/${examId}/inscriptions`);
  }

  registerToExam(examId: string, studentId: string): Observable<ExamInscription> {
    return this.http.post<ExamInscription>(`${API_URL}exams/${examId}/inscriptions`, { studentId });
  }

  setInscriptionStatus(inscriptionId: string, status: InscriptionStatus): Observable<ExamInscription> {
    return this.http.patch<ExamInscription>(`${API_URL}exams/inscriptions/${inscriptionId}`, null, { params: { status } });
  }

  myExamInscriptions(): Observable<ExamInscription[]> {
    return this.http.get<ExamInscription[]>(`${API_URL}exams/inscriptions/me`);
  }

  // ─── Réservations ─────────────────────────────────────────────────
  reservations(page = 0, size = 50): Observable<Page<Reservation>> {
    return this.http.get<Page<Reservation>>(`${API_URL}reservations`, { params: paging(page, size) });
  }

  createReservation(request: ReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(`${API_URL}reservations`, request);
  }

  confirmReservation(id: string): Observable<Reservation> {
    return this.http.patch<Reservation>(`${API_URL}reservations/${id}/confirm`, {});
  }

  cancelReservation(id: string): Observable<Reservation> {
    return this.http.patch<Reservation>(`${API_URL}reservations/${id}/cancel`, {});
  }

  // ─── Paiements ────────────────────────────────────────────────────
  payments(page = 0, size = 50): Observable<Page<Payment>> {
    return this.http.get<Page<Payment>>(`${API_URL}payments`, { params: paging(page, size) });
  }

  paymentSummary(): Observable<PaymentSummary> {
    return this.http.get<PaymentSummary>(`${API_URL}payments/summary`);
  }

  /** Moniteur : paiement reçu à la caisse. Élève : paiement Mobile Money (Campay). */
  createPayment(request: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(`${API_URL}payments`, request);
  }

  /** Redemande le statut à l'agrégateur (utile si le webhook n'est pas encore arrivé). */
  refreshPayment(id: string): Observable<Payment> {
    return this.http.post<Payment>(`${API_URL}payments/${id}/refresh`, {});
  }

  validatePayment(id: string): Observable<Payment> {
    return this.http.patch<Payment>(`${API_URL}payments/${id}/validate`, {});
  }

  rejectPayment(id: string): Observable<Payment> {
    return this.http.patch<Payment>(`${API_URL}payments/${id}/reject`, {});
  }
}

function paging(page: number, size: number): HttpParams {
  return new HttpParams().set('page', page).set('size', size);
}
