package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "universities")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UniversityEntity {
    @Id
    private UUID uniId = UuidCreator.getTimeOrdered();
    private String uniName;
    private String address;
}