package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.Payment;
import cm.drivemaster.backend.models.dto.PaymentRequestDto;
import cm.drivemaster.backend.models.dto.PaymentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    @Mapping(source = "studentsId", target = "student.id")
    Payment fromRequestToEntity(PaymentRequestDto paymentRequestDto);
    PaymentResponseDto fromEntityToResponse(Payment payment);
}