package cm.mvtech.drivehub.modules.monitor.application.dto;

import cm.mvtech.drivehub.modules.enums.Gender;

import java.util.UUID;

/** Moniteur renvoyé par l'API (aucune donnée sensible du compte). */
public record MonitorResponseDto(
        UUID id,
        UUID userId,
        String firstname,
        String lastname,
        String email,
        String phoneNumber,
        Gender gender,
        String residenceCity
) {
}
