export enum AdminStatus {
    "ACTIVE" = "ACTIVE",
    "INACTIVE" = "INACTIVE",
    "PENDING" = "PENDING",
    "EMAIL_PENDING" = "EMAIL_PENDING",
    /** Compte désactivé par un ROOT (PATCH /admin/{id}/deactivate), réactivable. */
    "SUSPENDED" = "SUSPENDED"
}

export default AdminStatus;