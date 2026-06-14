package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import java.time.LocalDate;
import java.util.UUID;

public record DrivingSchoolRegistryPageDTO(
        UUID id,
        String schoolName,
        String city,
        String country,
        String address,
        String email,
        String phoneNumber,
        String whatsappNumber,
        String websiteUrl,
        String description,
        DrivingSchoolStatus drivingSchoolStatus,
        String monitorName,
        String monitorPhone,
        LocalDate createdAt
) {}