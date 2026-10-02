package cm.mvtech.drivehub.platform.mailing.domain.model;

/** Destinataires possibles d'un email envoyé depuis le back-office. */
public enum EmailAudience {
    /** Adresse de contact de chaque auto-école validée (APPROVED ou ACTIVE). */
    ALL_SCHOOLS,
    /** Tous les comptes moniteurs (sauf suspendus). */
    ALL_MONITORS,
    /** Tous les comptes élèves (sauf suspendus). */
    ALL_STUDENTS,
    /** Le responsable et les membres acceptés (élèves, moniteurs) d'une auto-école donnée. */
    SCHOOL_MEMBERS,
    /** Une liste d'adresses choisies (1 à 50). */
    INDIVIDUAL
}
