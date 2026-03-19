package cm.mvtech.drivehub.platform.admin.enums;

public enum AdminRole {

    REVIEWER("approuve/rejete uniquement les demande des moniteur"),
    ROOT("possede le controle total sur tout les schema de plateforme"),
    SUPER_ADMIN("peu approuve, et suspendre des auto_ecole en cas de non conformité"),
    ;

    private String value;

    AdminRole(String value) {
        this.value = value;
    }
}
