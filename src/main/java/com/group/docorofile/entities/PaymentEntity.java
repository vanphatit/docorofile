package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EPaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PaymentEntity {
    @Id
    private UUID paymentId = UuidCreator.getTimeOrdered();
    private double amount;
    private String currency;
    private Date paymentDate;
    @Enumerated(EnumType.STRING)
    private EPaymentStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}