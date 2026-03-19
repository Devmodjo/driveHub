package cm.mvtech.drivehub.modules.payment;

import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentMotif;
import cm.mvtech.drivehub.modules.enums.PaymentStatus;
import cm.mvtech.drivehub.modules.student.StudentsResponseDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PaymentResponseDto(
        @NotNull
        Long id,
        @NotNull
        DrivingSchoolResponseDto drivingSchool,
        @NotNull
        StudentsResponseDto student,
        @NotNull
        Double amount,
        @NotNull
        PaymentMethod method,
        @NotNull
        PaymentMotif motif,
        @NotNull
        PaymentStatus paymentStatus,
        @NotNull
        LocalDate datePayment
) {
}
