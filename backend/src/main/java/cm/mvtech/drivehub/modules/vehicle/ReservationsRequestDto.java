package cm.mvtech.drivehub.modules.vehicle;

import cm.mvtech.drivehub.modules.enums.ReservationStatus;
import cm.mvtech.drivehub.modules.enums.ReservationTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


public record ReservationsRequestDto(

        @Positive
        @NotBlank(message = "l'identtifiant de l'auto ecole est obligatoire")
        Long drivingSchoolId,

        @Positive
        @NotBlank(message = "l'identtifiant de l'étudiant est obligatoire")
        Long studentId,

        @Positive
        @NotBlank(message = "l'identtifiant de l'encadreur est obligatoire")
        Long monitorId,

        @NotBlank(message = "le type de reservation  est obligatoire")
        ReservationTypes types,

        @NotBlank(message = "l'identtifiant de l'auto ecole est obligatoire")
        ReservationStatus reservationStatus
) {
}
