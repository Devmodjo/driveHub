import { ProjectStateEnum, RequestStatus, State } from "../additional-models/State"
/**
 * @author Basile Fofack
 * @email juniorbasilefofack@gmail.com
 */

export const STATE: State = {
  STATE_ACTIVATED: 0,
  STATE_DELETED: 2,
  STATE_ARCHIVE: 1,
  STATE_DEACTIVATED: 3,
  STATE_LOCKED: 4,
  STATE_BANNED: 5,
  STATE_MAIL_SENDED: 6,
}

export const REQUEST_STATUS: RequestStatus = {
  PENDING: 'PENDING',
  REJECTED: 'REJECTED',
  APPROUVED: 'APPROUVED'
}

export const PROJECT_STATUS: ProjectStateEnum = {
  STARTED: 'STARTED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  INPROGRESS: 'IN_PROGRESS'
}

export const PROJECT_STATUS_FR: ProjectStateEnum = {
  STARTED: 'Créé',
  COMPLETED: 'Terminé',
  CANCELLED: 'En pause',
  INPROGRESS: 'En cours'
}

export const REQUEST_STATUS_FR: RequestStatus = {
  PENDING: 'En Attente',
  REJECTED: 'Rejetée(s)',
  APPROUVED: 'Approuvée(s)'
}

export const ROLES = {
  SUPER_ADMIN_ROLE: 'SUPER_ADMIN',
  ADMIN_ROLE: 'ADMIN',
  USER_ROLE: 'USER',
  RECRUITER_ROLE: 'RECRUITER',
  ENTERPRISE_ROLE: 'ENTERPRISE',
  JOBER_ROLE: 'JOBER'
}

export const ROLES_FR = {
  SUPER_ADMIN_ROLE: 'Super administrateur',
  ADMIN_ROLE: 'Administrateur',
  USER_ROLE: 'Utilisateur',
  CLIENT_ROLE: 'Client',
  ENGINEER_ROLE: 'Maçon'
}

export const MODAL_MODE = {
  update: 'UPDATE',
  create: 'CREATE'
}

export const EXCEPTION = {
  USER_NOT_FOUND: "User not found",
  USER_NOT_FOUND_CODE: "617",

  PERMISSION_NOT_FOUND: "Permission not found",
  ROLE_NOT_FOUND: "Role not found",
  USER_AND_ROLE_NOT_FOUND: "User and role not found",
  ROLE_AND_PERMISSION_NOT_FOUND: "role and permission not found",
  ROLE_PERMISSION_NOT_FOUND: "Role-permission not found",
  USER_ROLE_NOT_FOUND: "User-role not found",
  USERNAME_AND_EMAIL_ALREADY_EXIST: "Username and email already exist",

  USERNAME_ALREADY_EXIST: "Username already exist",
  EMAIL_ALREADY_EXIST: "Email already exist",
  EMAIL_IS_NOT_CORRECT: "Email is not correct",
  EMAIL_ALREADY_USED_CODE: "612",
  EMAIL_ALREADY_USED: "Email already used",
  USERNAME_ALREADY_USED_CODE: "613",
  ROLE_NAME_ALREADY_EXIST: "Role name already exist",
  PERMISSION_NAME_ALREADY_EXIST: "Permission name already exist",

  USER_ALREADY_DELETED: "User already deleted",
  PERMISSION_ALREADY_DELETED: "Permission already deleted",
  ROLE_ALREADY_DELETED: "Role already deleted",

  USER_ALREADY_DEACTIVATED: "User already deactivated",
  ROLE_ALREADY_DEACTIVATED: "Role already deactivated",
  PERMISSION_ALREADY_DEACTIVATED: "Permission already deactivated",

  INVALID_INPUT_STRING: "Bad request",
  COULD_NOT_READ_FILE: "Could not read file",
  COULD_NOT_DELETE_FILE: "Could not process the deletion",
  COULD_NOT_DOWNLOAD_FILE: "Could not download file",
  COULD_NOT_SEND_MAIL: "Could not download file",

  NOT_ACTIVE_USERS: "There is not active user",
  OLD_PASSWORD_NOT_MATCH: "Old password do not match",

  ROLE_PERMISSION_ALREADY_EXIST: "role-permission already exist",
  USER_ROLE_ALREADY_EXIST: "User-role already exist",

  RUN_TIME_EXCEPTION: "Malformed URL has occurred",
  USER_IS_NULL: "Value of user is null",
  ROLE_IS_NULL: "Value of role is null",
  PERMISSION_IS_NULL: "Value of permission is null",
  USER_NOT_AUTHORIZED_CODE: "619",
  USER_NOT_AUTHORIZED: "You are not authorize to create an admin",

  NO_INTERNET_CONNECTION: "An unknown error has occurred please check your internet connection",
  UNKNOWN_ERROR_STATUS: "Unknown error: Unable to change the status",
  ERROR_CODE: 500,
  NO_AUTORISATION_PERMISSION_CREATED: "You are not authorized to create a permission",
  NO_AUTORISATION_CHANGE_PERMISSION: "You are not authorized to change a permission",
  NO_AUTORISATION_ROLE_CREATED: "You are not authorized to create a ROLE",

  NO_AUTORISATION_CHANGE_ROLE: "You are not authorized to change a role",
  CREATE_SUCCESSFULLY: "Created successlly",
  DELETE_SUCCESSFULLY: "Deleted successlly",
  DEACTIVATE_SUCCESSFULLY: "Deactivated successlly",
  ACTIVATE_SUCCESSFULLY: "Activated successlly",
  UPDATE_SUCCESSFULLY: "Updated successlly",

  UNIQUE_USER_CLIENT: "A user is already associated with this client",
  UNIQUE_USER_CLIENT_CODE: "620",

  UNIQUE_USER_ENGINEER: "A user is already associated with this engineer",
  UNIQUE_USER_ENGINEER_CODE: "623",

  PROJECT_PARTICIPATION_STEP_NOT_FOUND: "Project participation step not found",
  PROJECT_PARTICIPATION_STEP_NOT_FOUND_CODE: "628",

  PROJECT_PARTICIPATION_ALREADY_EXIST: "Project participation already exist",
  PROJECT_PARTICIPATION_ALREADY_EXIST_CODE: "633",
  SETTING_MUST_NOT_BE_NULL: "Settings must not be null",

  USER_NOT_AUTHORIZED_TO_CREATE_JOBOFFER: "This user is not authorised to create a job offer",
  SKILL_NAME_ALREADY_EXIST: "Skill name already exist",
  EDUCATION_REQUIREMENT_NAME_ALREADY_EXIST: "Education requirement name already exist",
  LANGUAGE_REQUIREMENT_NAME_ALREADY_EXIST: "Language requirement name already exist",
  JOB_OFFER_CATEGORY_NAME_ALREADY_EXIST: "job offer category name already exist",
  JOB_OFFER_DEADLINE_ERROR: "Deadline must be greater than started date",
  JOB_OFFER_NOT_FOUND: "Job offer not found",
  NOT_ACCEPTABLE_CODE: "406 NOT_ACCEPTABLE",
  PRECONDITION_FAILED_CODE: "412 PRECONDITION_FAILED",
  ITEM_ALREADY_EXIST: "501",
  ITEM_NOT_FOUND: "403",
  JOB_OFFER_CATEGORY_NOT_FOUND: "Job offer category not found",
  USER_ACCOUNT_ALREADY_LOCKED: "User Account already locked.",
  USER_ACCOUNT_ALREADY_LOCKED_CODE: "644",

  USER_ACCOUNT_ALREADY_BANED: "User Account already baned",
  USER_ACCOUNT_ALREADY_BANED_CODE: "645",
  JOBER_NOT_FOUND: "Client not found",
  JOBER_ALREADY_APPLIED_JOB_OFFER: "Jober already applied for this job",

  JOB_OFFER_NOT_FOUND_CODE: "646",
  JOB_OFFER_ALREADY_LOCKED: "Job offer already locked",
  JOB_OFFER_ALREADY_LOCKED_CODE: "647",

  OLD_PASSWORD_NOT_MATCH_CODE: "618",

  INVALID_USER_EMAIL: "No user is associated with this email address",
  INVALID_USER_EMAIL_CODE: "646",

  RESET_TOKEN_EXPIRED: "The reset token has expired",
  RESET_TOKEN_EXPIRED_CODE: "650",

  EMPTY_CACHE: "Cache is Empty",
  EMPTY_CACHE_CODE: "648",

  USER_ALREADY_ARCHIVE: "User already archive",
  USER_ALREADY_ARCHIVE_CODE: "647",
  USER_ALREADY_DEACTIVATED_CODE: "616",
  USER_ALREADY_DELETED_CODE: "615",

  JOB_APPLICATION_IS_EMPTY: "Job application is empty",
  JOB_APPLICATION_IS_EMPTY_CODE: "653",

  JOB_OWNER_IS_EMPTY: "The prestataire is null",
  JOB_OWNER_IS_EMPTY_CODE: "654",

  MARK_ID_IS_NULL_CODE: "655",
  MARK_DOES_NOT_EXISTS_CODE: "656",

  NEWS_LETTER_NOT_FOUND: "The news letter does not exists",
  NEWS_LETTER_NOT_FOUND_CODE: "661",

  NO_USER_IS_SUBSCRIBED_TO_NEWS_LETTER: "No user is subscribed to the newsletter",
  NO_USER_IS_SUBSCRIBED_TO_NEWS_LETTER_CODE: "662",
  SUPER_ADMIN_IS_UNABLE_TO_UPDATE_ADMIN: "Modification of this administrator is not permitted as you are not the creator.",
  SUPER_ADMIN_IS_UNABLE_TO_UPDATE_ADMIN_CODE: "660"

}
