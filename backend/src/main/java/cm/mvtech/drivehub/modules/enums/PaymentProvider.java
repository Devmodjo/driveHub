package cm.mvtech.drivehub.modules.enums;

/** Origine d'un paiement. */
public enum PaymentProvider {
    /** Enregistré à la main par le moniteur (espèces, virement constaté...). */
    MANUAL,
    /** Passerelle simulée (développement) : aucun argent ne circule. */
    SIMULATED,
    /** Campay : Mobile Money MTN / Orange au Cameroun. */
    CAMPAY
}
