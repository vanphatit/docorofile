package com.group.docorofile.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "favorite_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID favId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member; // Người dùng đã yêu thích tài liệu này

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document; // Mỗi lượt yêu thích chỉ liên kết với một tài liệu

    private LocalDateTime createdOn;

    @PrePersist
    public void prePersist() {
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}