package com.group.docorofile.repositories;

import com.group.docorofile.entities.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    @Query("SELECT c FROM CommentEntity c WHERE c.document.documentId = :documentId")
    List<CommentEntity> findCommentEntitiesByDocument_DocumentId (UUID documentId);

}
