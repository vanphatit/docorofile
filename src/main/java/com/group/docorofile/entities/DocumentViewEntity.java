package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_views")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class DocumentViewEntity implements Serializable {
    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    private LocalDateTime viewedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UuidCreator.getTimeOrdered();
        }
        if (this.viewedAt == null) {
            this.viewedAt = LocalDateTime.now();
        }
    }
}
