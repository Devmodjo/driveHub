package cm.drivemaster.backend.models.dto;

import java.time.LocalDateTime;

public record ErrorDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
