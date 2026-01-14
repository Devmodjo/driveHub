package cm.drivemaster.backend.models.dto;

public record ApiResponse(
        boolean success,
        String message
) {
}
