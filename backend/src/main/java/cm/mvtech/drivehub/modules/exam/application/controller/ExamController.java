package cm.mvtech.drivehub.modules.exam.application.controller;

import cm.mvtech.drivehub.modules.enums.InscriptionStatus;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsInscriptionRequestDto;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsInscriptionResponseDto;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsRequestDto;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsResponseDto;
import cm.mvtech.drivehub.modules.exam.domain.services.ExamService;
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

import java.util.List;
import java.util.UUID;

/** Examens et inscriptions (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Tag(name = "EXAM API", description = "sessions d'examen et inscriptions des élèves (tenant)")
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "liste des sessions d'examen")
    @GetMapping
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public ApiPageResponse<ExamsResponseDto> list(
            @PageableDefault(size = 20, sort = "dateExams", direction = Sort.Direction.ASC) Pageable pageable) {
        return examService.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MONITOR')")
    public ExamsResponseDto create(@Valid @RequestBody ExamsRequestDto request) {
        return examService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public ExamsResponseDto update(@PathVariable UUID id, @Valid @RequestBody ExamsRequestDto request) {
        return examService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public ApiResponse delete(@PathVariable UUID id) {
        examService.delete(id);
        return new ApiResponse(true, "Examen supprimé");
    }

    @Operation(summary = "inscrire un élève à l'examen")
    @PostMapping("/{examId}/inscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MONITOR')")
    public ExamsInscriptionResponseDto register(@PathVariable UUID examId,
                                                @Valid @RequestBody ExamsInscriptionRequestDto request) {
        return examService.register(examId, request.studentId());
    }

    @Operation(summary = "élèves inscrits à l'examen")
    @GetMapping("/{examId}/inscriptions")
    @PreAuthorize("hasRole('MONITOR')")
    public List<ExamsInscriptionResponseDto> inscriptions(@PathVariable UUID examId) {
        return examService.inscriptionsOfExam(examId);
    }

    @Operation(summary = "valider ou refuser une inscription")
    @PatchMapping("/inscriptions/{inscriptionId}")
    @PreAuthorize("hasRole('MONITOR')")
    public ExamsInscriptionResponseDto updateStatus(@PathVariable UUID inscriptionId,
                                                    @RequestParam InscriptionStatus status) {
        return examService.updateInscriptionStatus(inscriptionId, status);
    }

    @Operation(summary = "mes inscriptions (élève connecté)")
    @GetMapping("/inscriptions/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ExamsInscriptionResponseDto> myInscriptions() {
        return examService.myInscriptions();
    }
}
