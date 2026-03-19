package cm.mvtech.drivehub.modules.enums;

public enum InscriptionStatus {

    INSCRIT("inscription validée"),
    REFUSE("inscription rejeter"),
    ;

    private String value;

    InscriptionStatus(String value) {
        this.value = value;
    }
}
