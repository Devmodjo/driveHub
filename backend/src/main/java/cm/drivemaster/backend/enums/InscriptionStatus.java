package cm.drivemaster.backend.enums;

public enum InscriptionStatus {

    INSCRIT("inscription validée"),
    REFUSE("inscription rejeter"),
    ;

    private String value;

    InscriptionStatus(String value) {
        this.value = value;
    }
}
