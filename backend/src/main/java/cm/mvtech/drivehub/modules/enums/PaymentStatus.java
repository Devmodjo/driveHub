package cm.mvtech.drivehub.modules.enums;

public enum PaymentStatus {
    PENDING("en attentes..."),
    VALIDATE("paiement validé"),
    REJECTED("paiement rejeté");

    private String value;

    PaymentStatus(String value) {
        this.value = value;
    }
}
