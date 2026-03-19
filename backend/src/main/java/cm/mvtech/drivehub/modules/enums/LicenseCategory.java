package cm.mvtech.drivehub.modules.enums;

public enum LicenseCategory {

    A("moto, motocyclette"),
    B("Voitures, véhicules légers"),
    C("Camions"),
    D("Bus, Transport de personnes"),
    E("Tracteur, Engin agricole"),
    G("engin de chantier");

    private String value;

    LicenseCategory(String value) {
        this.value = value;
    }
}
