package com.group.docorofile.repositories;

import com.group.docorofile.entities.DocumentViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentViewRepository extends JpaRepository<DocumentViewEntity, UUID> {
    boolean existsByDocument_DocumentIdAndMember_UserId(UUID documentId, UUID memberId);

    @Query("SELECT dv.document.documentId FROM DocumentViewEntity dv WHERE dv.member.userId = :memberId")
    List<UUID> findViewedDocumentsByMemberId (UUID memberId);
}
