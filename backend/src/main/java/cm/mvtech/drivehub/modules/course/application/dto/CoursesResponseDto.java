package cm.mvtech.drivehub.modules.course.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CoursesResponseDto(
        UUID id,
        String title,
        String content,
        LocalDate createdAt
) {
}
