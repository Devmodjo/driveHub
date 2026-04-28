package cm.mvtech.drivehub.modules.student;

import cm.mvtech.drivehub.modules.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record StudentRegisterRequest(
        @Schema(defaultValue = "john")
        String firstname,
        @Schema(defaultValue = "Doe")
        String lastname,
        @Schema(defaultValue = "john.doe@gmail.com")
        String email,
        @Schema(defaultValue = "pass123")
        String password,
        @Schema(defaultValue = "+237689078576")
        String phoneNumber,
        @Schema(defaultValue = "MALE")
        Gender gender,
        @Schema(defaultValue = "Cameroon")
        String nationality,
        @Schema(defaultValue = "Yaoundé, Bastos")
        String residenceCity,
        @Schema(defaultValue = "1999-01-01")
        Date dateOfBirth

) {}
