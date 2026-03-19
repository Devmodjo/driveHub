package cm.mvtech.drivehub.modules.enums;

public enum DrivingSchoolStatus {
    APPROVED("creation de l'auto-ecole approuver par les admins"),
    ACTIVE("auto-école active et affichier dans la liste des auto école"),
    PENDING("auto-école en attente"),
    SUSPENDED("auto-école suspendu");

    private String v;

    DrivingSchoolStatus(String v) {
        this.v = v;
    }
}
