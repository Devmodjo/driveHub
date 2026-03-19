package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.Gender;

public record DrivingSchoolPendingRequestDTO(
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
