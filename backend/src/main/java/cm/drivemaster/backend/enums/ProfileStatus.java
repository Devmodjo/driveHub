package cm.drivemaster.backend.enums;

public enum ProfileStatus {

    ACTIVE("profile actif"),
    PENDING("profile suspendu"),
    INACTIVE("profile inactif");

    private String value;

    ProfileStatus(String value) {
        this.value = value;
    }
}
