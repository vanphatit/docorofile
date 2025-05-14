package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "universities")
public class UniversityEntity implements Serializable {
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