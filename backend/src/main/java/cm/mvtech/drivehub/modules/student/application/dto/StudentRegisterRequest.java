package cm.mvtech.drivehub.modules.student.application.dto;

import cm.mvtech.drivehub.modules.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Date;

public record StudentRegisterRequest(
        @Schema(defaultValue = "john")
        @NotBlank String firstname,
        @Schema(defaultValue = "Doe")
        String lastname,
        @Schema(defaultValue = "john.doe@gmail.com")
        @NotBlank @Email String email,
        @Schema(defaultValue = "Pass1234")
        @NotBlank @Size(min = 8, message = "le mot de passe doit contenir au moins 8 caractères") String password,
        @Schema(defaultValue = "+237689078576")
        @NotBlank String phoneNumber,
        @Schema(defaultValue = "MALE")
        @NotNull Gender gender,
        @Schema(defaultValue = "Cameroon")
        @NotBlank String nationality,
        @Schema(defaultValue = "Yaoundé, Bastos")
        @NotBlank String residenceCity,
        @Schema(defaultValue = "1999-01-01")
        @NotNull @Past Date dateOfBirth

) {}
