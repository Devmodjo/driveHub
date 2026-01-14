package cm.drivemaster.backend.models.dto;

public record StudentRegisterRequest(
        String name,
        String email,
        String password
) {}
