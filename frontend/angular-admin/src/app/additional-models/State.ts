export interface State {
  STATE_ACTIVATED: number;
  STATE_DELETED: number;
  STATE_ARCHIVE: number;
  STATE_DEACTIVATED: number;
  STATE_LOCKED: number;
  STATE_BANNED: number;
  STATE_MAIL_SENDED: number;
}

export interface RequestStatus {
  PENDING: string;
  REJECTED: string;
  APPROUVED: string;
}

export interface ProjectStateEnum {
  STARTED: string;
  COMPLETED: string;
  CANCELLED: string;
  INPROGRESS: string;
}
