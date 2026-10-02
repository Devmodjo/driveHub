/**
 * Demande de création d'auto-école en attente d'approbation
 * (GET /api/platform/registries/pending, record DrivingSchoolPendingRequestDTO du backend).
 * L'identifiant est celui du registre : il sert aux actions approve / reject.
 */
export interface PendingSchoolRequest {
  id: string;
  schoolName: string;
  country: string;
  city: string;
  address: string;
  whatsappNumber: string;
  monitorName: string;
  monitorResidence: string;
  monitorNationality: string;
  gender: string;
}
