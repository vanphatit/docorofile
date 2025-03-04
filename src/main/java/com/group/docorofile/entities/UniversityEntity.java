package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "universities")
public class UniversityEntity {
    @Id
    private UUID univId;

    private String univName;
    private String address;

    @PrePersist
    public void prePersist() {
        if (this.univId == null) {
            this.univId = UuidCreator.getTimeOrdered();
        }
    }
}