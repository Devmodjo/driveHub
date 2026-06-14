package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;

import java.time.LocalDate;
import java.util.UUID;

public record DrivingSchoolRegistryDetailDTO(
        UUID id,
        String schoolName,
        String schemaName,
        String country,
        String city,
        String address,
        String email,
        String phoneNumber,
        String whatsappNumber,
        String websiteUrl,
        String description,
        DrivingSchoolStatus drivingSchoolStatus,
        String monitorName,
        String monitorResidence,
        String monitorNationality,
        String monitorGender,
        String monitorPhone,
        LocalDate createdAt
) {
}
