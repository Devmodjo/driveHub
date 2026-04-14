package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import cm.mvtech.drivehub.modules.enums.Gender;

import java.util.UUID;

public record DrivingSchoolPendingRequestDTO(
        UUID id,
        String schoolName,
        String country,
        String city,
        String address,
        String whatsappNumber,
        String monitorName,
        String monitorResidence,
        String monitorNationality,
        Gender gender
) {
}
