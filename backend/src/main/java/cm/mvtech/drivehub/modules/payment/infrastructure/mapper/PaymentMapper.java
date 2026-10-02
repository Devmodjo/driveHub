package cm.mvtech.drivehub.modules.payment.infrastructure.mapper;

import cm.mvtech.drivehub.modules.payment.application.dto.PaymentResponseDto;
import cm.mvtech.drivehub.modules.payment.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.user.firstname", target = "studentFirstname")
    @Mapping(source = "student.user.lastname", target = "studentLastname")
    PaymentResponseDto fromEntityToResponse(Payment payment);
}
