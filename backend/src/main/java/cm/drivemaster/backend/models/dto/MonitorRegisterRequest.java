package cm.drivemaster.backend.models.dto;

public record MonitorRegisterRequest(
        String name,
        String email,
        String password,
        String phoneNumber
) {}
