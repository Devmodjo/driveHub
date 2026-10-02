package cm.mvtech.drivehub.modules.course.application.controller;

import cm.mvtech.drivehub.modules.course.application.dto.CoursesRequestDto;
import cm.mvtech.drivehub.modules.course.application.dto.CoursesResponseDto;
import cm.mvtech.drivehub.modules.course.domain.services.CourseService;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Cours de l'auto-école (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "COURSE API", description = "cours publiés par l'auto-école (tenant)")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "liste des cours")
    @GetMapping
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public ApiPageResponse<CoursesResponseDto> list(
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {
        return courseService.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public CoursesResponseDto get(@PathVariable UUID id) {
        return courseService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MONITOR')")
    public CoursesResponseDto create(@Valid @RequestBody CoursesRequestDto request) {
        return courseService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public CoursesResponseDto update(@PathVariable UUID id, @Valid @RequestBody CoursesRequestDto request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public ApiResponse delete(@PathVariable UUID id) {
        courseService.delete(id);
        return new ApiResponse(true, "Cours supprimé");
    }
}
