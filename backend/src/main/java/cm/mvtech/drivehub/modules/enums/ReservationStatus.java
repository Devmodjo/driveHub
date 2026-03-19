package cm.mvtech.drivehub.modules.enums;

public enum ReservationStatus {

    PENDING("réservation en attente"),
    CONFIRMED("reservation confirmée"),
    CANCELLED("réservation annulée");

    private String value;

    ReservationStatus(String value) {
        this.value = value;
    }
}
