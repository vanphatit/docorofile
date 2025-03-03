package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "transaction_logs")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TransactionLogEntity {
    @Id
    private UUID transId = UuidCreator.getTimeOrdered();
    private String action;
    private Date createdOn;
    private String detail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}