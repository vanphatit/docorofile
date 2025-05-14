package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EPaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class PaymentEntity implements Serializable {
    @Id
    private UUID paymentId;

    private double amount;

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