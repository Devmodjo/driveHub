package cm.mvtech.drivehub.modules.vehicle;

import cm.mvtech.drivehub.modules.enums.ReservationStatus;
import cm.mvtech.drivehub.modules.enums.ReservationTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;


public record ReservationsRequestDto(

        @Positive
        @NotBlank(message = "l'identtifiant de l'auto ecole est obligatoire")
        UUID drivingSchoolId,

        @Positive
        @NotBlank(message = "l'identtifiant de l'étudiant est obligatoire")
        UUID studentId,

        @Positive
        @NotBlank(message = "l'identtifiant de l'encadreur est obligatoire")
        UUID monitorId,

        @NotBlank(message = "le type de reservation  est obligatoire")
        ReservationTypes types,

        @NotBlank(message = "l'identtifiant de l'auto ecole est obligatoire")
        ReservationStatus reservationStatus
) {
}
