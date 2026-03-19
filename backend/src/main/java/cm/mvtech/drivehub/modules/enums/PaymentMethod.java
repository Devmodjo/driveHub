package cm.mvtech.drivehub.modules.enums;

public enum PaymentMethod {

    MOMO("Mobile money"),
    OM("Orange money"),
    CASH("à la caisse");

    private String value;

    PaymentMethod(String value) {
        this.value = value;
    }
}
