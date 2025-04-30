package com.group.docorofile.models.dto;

import com.group.docorofile.enums.EPaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentTableDTO {
    private UUID id;
    private double amount;
    private EPaymentStatus status;
    private LocalDateTime paymentDate;

    public static PaymentTableDTO fromEntity(com.group.docorofile.entities.PaymentEntity entity) {
        PaymentTableDTO dto = new PaymentTableDTO();
        dto.setId(entity.getPaymentId());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setPaymentDate(entity.getPaymentDate());
        return dto;
    }
}