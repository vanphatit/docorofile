package com.group.docorofile.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class PaymentDTO {
    @Data
    @Builder
    public static class VNPayResponse {
        public String code;
        public String message;
        public String paymentUrl;
    }

    @Data
    @Builder
    public static class PaymentResponse {
        private UUID paymentId;
        private double amount;
        private String status;
        private LocalDateTime paymentDate;
        private String payerFullName;
        private String payerEmail;
    }

    @Data
    @Builder
    public static class PaymentRequest {
        private double amount;         // Số tiền thanh toán
        private String upgradeType;    // Gói nâng cấp: "PREMIUM", "3_MONTHS",...
    }
}
