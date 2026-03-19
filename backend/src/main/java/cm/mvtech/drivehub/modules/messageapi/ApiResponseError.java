package cm.mvtech.drivehub.modules.messageapi;

import java.time.LocalDateTime;

public record ApiResponseError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
