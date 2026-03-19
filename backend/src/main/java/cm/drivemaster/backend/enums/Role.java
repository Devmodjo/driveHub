package cm.drivemaster.backend.enums;

public enum Role {
    // ADMIN("administrateur"),
    MONITOR("encadreur"),
    STUDENT("étudiant");

    private String value;

    Role(String value) {
        this.value = value;
    }
}
