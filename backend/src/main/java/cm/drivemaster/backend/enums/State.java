package cm.drivemaster.backend.enums;

public enum State {
    DISPOSABLE("vehicule disponible"),
    PANNE("engin en panne"),
    MAINTENANCE("véhicule en panne");

    private String value;

    State(String value) {
        this.value = value;
    }
}
