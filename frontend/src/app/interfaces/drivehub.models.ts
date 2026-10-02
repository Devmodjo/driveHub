/**
 * Types de l'espace auto-école / élève.
 * Chaque interface reprend exactement un DTO (record Java) du backend : garder les deux synchronisés.
 */

export type UserRole = 'MONITOR' | 'STUDENT';
export type ProfileStatus = 'REGISTERED' | 'EMAIL_VERIFIED' | 'PROFILE_COMPLETED' | 'ACTIVE' | 'SUSPENDED' | 'PENDING';
export type Gender = 'MALE' | 'FEMALE';
export type LicenseCategory = 'A' | 'B' | 'C' | 'D' | 'E' | 'G';
export type JoinStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type SchoolStatus = 'APPROVED' | 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'REJECTED';
export type VehicleState = 'DISPOSABLE' | 'PANNE' | 'MAINTENANCE';
export type ReservationType = 'CONDUITE' | 'RENDEZVOUS' | 'ADMIN';
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';
export type PaymentMethod = 'MOMO' | 'OM' | 'CASH';
export type PaymentMotif = 'INSCRIPTION' | 'EXAMS';
export type PaymentStatus = 'PENDING' | 'VALIDATE' | 'REJECTED';
export type PaymentProvider = 'MANUAL' | 'SIMULATED' | 'CAMPAY';
export type InscriptionStatus = 'INSCRIT' | 'REFUSE';

/** Réponse paginée commune (ApiPageResponse). */
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiMessage {
  success: boolean;
  message: string;
}

// ─── Authentification ───────────────────────────────────────────────
export interface AuthResponse {
  id: string;
  token: string;
  role: UserRole;
  profileStatus: ProfileStatus;
  fullProfile: boolean;
}

export interface CurrentUser {
  id: string;
  firstname: string;
  lastname: string;
  email: string;
  roles: UserRole;
  profileStatus: ProfileStatus;
  createdAt: string;
  fullprofile: boolean;
}

/** Inscription : même formulaire pour élève (/register/student) et moniteur (/register/monitor). */
export interface RegisterRequest {
  firstname: string;
  lastname: string;
  email: string;
  password: string;
  phoneNumber: string;
  gender: Gender;
  nationality: string;
  residenceCity: string;
  dateOfBirth: string; // yyyy-MM-dd
  /** Acceptation de la politique de confidentialité : obligatoire (sinon le backend refuse l'inscription). */
  acceptPrivacyPolicy: boolean;
}

// ─── Auto-écoles (schéma public) ────────────────────────────────────
export interface PublicSchool {
  id: string;
  name: string;
  phoneNumber: string;
  address: string;
  email: string;
  country: string;
  city: string;
  createdAt: string;
  whatsappNumber: string;
  websiteUrl: string;
  drivingSchoolStatus: SchoolStatus;
}

export interface SchoolRequest {
  name: string;
  email: string;
  country: string;
  city: string;
  phoneNumber: string;
  address: string;
  description: string;
  websiteUrl: string;
  whatsappNumber: string;
}

export interface MySchoolRegistry {
  id: string;
  schoolName: string;
  city: string;
  country: string;
  address: string;
  email: string;
  phoneNumber: string;
  whatsappNumber: string;
  websiteUrl: string;
  description: string;
  drivingSchoolStatus: SchoolStatus;
  monitorName: string;
  monitorPhone: string;
  createdAt: string;
}

export interface MyJoinRequest {
  requestId: string;
  drivingSchoolId: string;
  drivingSchoolName: string;
  city: string;
  joinStatus: JoinStatus;
  requestedAt: string;
}

export interface PendingJoinRequest {
  requestId: string;
  userId: string;
  userName: string;
  userEmail: string;
  role: UserRole;
  drivingSchoolId: string;
  drivingSchoolName: string;
  requestedAt: string;
}

// ─── Données d'une auto-école (schéma du tenant) ────────────────────
export interface Student {
  id: string;
  userId: string;
  firstname: string;
  lastname: string;
  email: string;
  phoneNumber: string;
  dateOfBirth: string;
  gender: Gender;
  nationality: string;
  residenceCity: string;
  cniRectoUrl: string | null;
  cniVersoUrl: string | null;
  licenseCategory: LicenseCategory | null;
  createdOn: string;
}

export interface StudentUpdate {
  cniRectoUrl: string | null;
  cniVersoUrl: string | null;
  licenseCategory: LicenseCategory;
}

export interface Monitor {
  id: string;
  userId: string;
  firstname: string;
  lastname: string;
  email: string;
  phoneNumber: string;
  gender: Gender;
  residenceCity: string;
}

export interface Vehicle {
  id: string;
  matriculation: string;
  model: string;
  state: VehicleState;
  createdOn: string;
}

export interface VehicleRequest {
  matriculation: string;
  model: string;
  state: VehicleState;
}

export interface Course {
  id: string;
  title: string;
  content: string;
  createdAt: string;
}

export interface CourseRequest {
  title: string;
  content: string;
}

export interface Exam {
  id: string;
  dateExams: string;
  category: LicenseCategory;
}

export interface ExamRequest {
  dateExams: string; // yyyy-MM-ddTHH:mm:ss
  category: LicenseCategory;
}

export interface ExamInscription {
  id: string;
  examId: string;
  dateExams: string;
  category: LicenseCategory;
  studentId: string;
  studentFirstname: string;
  studentLastname: string;
  inscriptionStatus: InscriptionStatus;
  registeredAt: string;
}

export interface Reservation {
  id: string;
  studentId: string;
  studentFirstname: string;
  studentLastname: string;
  monitorId: string;
  monitorFirstname: string;
  monitorLastname: string;
  vehicleId: string | null;
  vehicleMatriculation: string | null;
  dateTime: string;
  types: ReservationType;
  reservationStatus: ReservationStatus;
}

export interface ReservationRequest {
  studentId: string | null; // ignoré quand l'élève réserve pour lui-même
  monitorId: string;
  vehicleId: string | null;
  dateTime: string;
  types: ReservationType;
}

export interface Payment {
  id: string;
  studentId: string;
  studentFirstname: string;
  studentLastname: string;
  amount: number;
  method: PaymentMethod;
  motif: PaymentMotif;
  paymentStatus: PaymentStatus;
  provider: PaymentProvider;
  phoneNumber: string | null;
  externalReference: string | null;
  gatewayMessage: string | null;
  datePayment: string;
}

export interface PaymentRequest {
  studentsId: string | null; // ignoré quand l'élève paie pour lui-même
  amount: number;
  method: PaymentMethod;
  motif: PaymentMotif;
  phoneNumber: string | null;
}

export interface PaymentSummary {
  totalValidated: number;
  totalPending: number;
}

// ─── Justificatifs (pièce d'identité, CAPEC) ────────────────────────
// Contrat : docs/API-JUSTIFICATIFS.md

/** CNI : pièce d'identité (CNI ou passeport). CAPEC : certificat d'aptitude à l'enseignement de la conduite. */
export type DocumentType = 'CNI' | 'CAPEC';
/** PENDING : en vérification, VERIFIED : vérifié, REJECTED : refusé (voir reviewComment). */
export type DocumentStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';

/** Un justificatif envoyé par un utilisateur (DocumentResponse du backend). */
export interface UserDocument {
  id: string;
  type: DocumentType;
  status: DocumentStatus;
  /** Numéro masqué (ex : « ••••4521 »), null si l'utilisateur ne l'a pas renseigné. */
  documentNumberMasked: string | null;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedAt: string;
  /** Motif du refus, null sauf si le document a été refusé. */
  reviewComment: string | null;
}

/** GET /api/documents/requirements : pièces demandées selon le rôle, et celles qui manquent encore. */
export interface DocumentRequirements {
  required: DocumentType[];
  missing: DocumentType[];
}

/** Décision du back-office sur un justificatif (commentaire obligatoire en cas de refus). */
export interface DocumentReview {
  status: 'VERIFIED' | 'REJECTED';
  comment: string | null;
}

// ─── Ajout d'un moniteur par le responsable de l'auto-école ─────────
/** Partie « monitor » (JSON) du formulaire multipart de POST /api/monitors. */
export interface MonitorCreateRequest {
  firstname: string;
  lastname: string;
  email: string;
  phoneNumber: string;
  gender: Gender;
  nationality: string;
  residenceCity: string;
  dateOfBirth: string; // yyyy-MM-dd
}

/** POST /api/auth/accept-invitation : le moniteur invité choisit son mot de passe. */
export interface AcceptInvitationRequest {
  token: string;
  password: string;
  acceptPrivacyPolicy: boolean;
}

// ─── Abonnement de l'auto-école ─────────────────────────────────────
/**
 * Seule la valeur TRIAL (période d'essai) est utilisée par le frontend pour l'instant.
 * Les autres valeurs seront précisées avec la facturation (docs/ABONNEMENTS-ET-PAIEMENTS.md côté backend).
 */
export type SubscriptionStatus = 'TRIAL' | (string & {});

/** GET /api/driving-schools/me/subscription (204 si l'auto-école n'est pas encore validée). */
export interface SchoolSubscription {
  planCode: string;
  planName: string;
  monthlyPriceFcfa: number;
  status: SubscriptionStatus;
  trialEndsAt: string | null;
  trialDaysLeft: number | null;
  billingEnabled: boolean;
}
