package cm.drivemaster.backend.enums;

public enum Statut {

    ACTIVE("profile actif"),
    PENDING("profile suspendu"),
    INACTIVE("profile inactif");

    private String value;

    Statut(String value) {
        this.value = value;
    }
}
