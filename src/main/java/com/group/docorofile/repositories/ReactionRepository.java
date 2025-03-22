package com.group.docorofile.repositories;

import com.group.docorofile.entities.ReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<ReactionEntity, UUID> {
    // Tìm phản ứng của một người dùng trên một tài liệu
    @Query("SELECT r FROM ReactionEntity r WHERE r.author.userId = :userId AND r.document.documentId = :documentId")
    Optional<ReactionEntity> findByUserAndDocument(UUID userId, UUID documentId);

    @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.document.documentId = :documentId AND r.isLike = true")
    int countLikeByDocumentId(UUID documentId);

    @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.document.documentId = :documentId AND r.isDislike = true")
    int countDislikeByDocumentId(UUID documentId);
}
