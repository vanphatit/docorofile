package com.group.docorofile.repositories;

import com.group.docorofile.entities.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID>, JpaSpecificationExecutor<DocumentEntity> {
    @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE DATE(d.uploadedDate) = :date AND d.author.userId = :memberId AND d.status = 'PUBLIC'")
    int countDocumentUploadInDay (LocalDate date, UUID memberId);

    // Kiểm tra xem user đã tải lên tài liệu này chưa
    boolean existsByTitleAndAuthor_UserId(String title, UUID userId);

    // Tìm tài liệu trùng lặp của cùng một user
    List<DocumentEntity> findByTitleAndAuthor_UserId(String title, UUID memberId);

    @Query("SELECT d FROM DocumentEntity d WHERE d.course.courseId = :courseId")
    List<DocumentEntity> findByCourse_CourseId(UUID courseId);

    List<DocumentEntity> findByCourse_University_UnivName(String universityName);

    List<DocumentEntity> findByCourse_University_UnivNameAndCourse_CourseName(String universityName, String courseName);

    List<DocumentEntity> findByUploadedDate(LocalDateTime uploadedDate);

    List<DocumentEntity> findByStatus(String status);

    List<DocumentEntity> findByAuthor_UserId(UUID userId);

    Page<DocumentEntity> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}

