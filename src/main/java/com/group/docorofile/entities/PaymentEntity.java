package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EPaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PaymentEntity {
    @Id
    private UUID paymentId;

    private double amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private EPaymentStatus status;

    private LocalDateTime paymentDate;

    @ManyToOne(optional = false)
    private MemberEntity payer;

    @PrePersist
    public void prePersist() {
        if (this.paymentId == null) {
            this.paymentId = UuidCreator.getTimeOrdered();
        }
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
    }
}