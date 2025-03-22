package com.group.docorofile.services.specifications;

import com.group.docorofile.entities.DocumentEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentSpecification {
    public static Specification<DocumentEntity> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";

                // JOIN INNER JOIN to CourseEntity
                Join<Object, Object> courseJoin = root.join("course", JoinType.INNER);

                // JOIN INNER JOIN to UniversityEntity
                Join<Object, Object> universityJoin = courseJoin.join("university", JoinType.INNER);

                // Tim kiem theo title, description
                Predicate titlePredicate = criteriaBuilder.like(root.get("title"), likePattern);
                Predicate descriptionPredicate = criteriaBuilder.like(root.get("description"), likePattern);

                // Tim kiem theo courseName, description
                Predicate courseNamePredicate = criteriaBuilder.like(courseJoin.get("courseName"), likePattern);
                Predicate courseDescriptionPredicate = criteriaBuilder.like(courseJoin.get("description"), likePattern);

                // Tim kiem theo universityName, description
                Predicate universityNamePredicate = criteriaBuilder.like(universityJoin.get("univName"), likePattern);

                // OR
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate, courseNamePredicate, courseDescriptionPredicate, universityNamePredicate));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<DocumentEntity> filterDocuments(UUID courseId, UUID universityId, LocalDateTime uploadDate, boolean sortByViews, boolean sortByLikes, boolean sortByDisLike, String status, boolean isAdmin) {
        return (Root<DocumentEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // JOIN với CourseEntity
            Join<DocumentEntity, Object> courseJoin = root.join("course", JoinType.INNER);

            // JOIN với UniversityEntity
            Join<Object, Object> universityJoin = courseJoin.join("university", JoinType.INNER);

            // LEFT JOIN với ReactionEntity để đếm số like
            Join<DocumentEntity, Object> reactionJoin = root.join("reactions", JoinType.LEFT);

            // Lọc theo khóa học
            if (courseId != null) {
                predicates.add(cb.equal(courseJoin.get("courseId"), courseId));
            }

            // Lọc theo trường đại học
            if (universityId != null) {
                predicates.add(cb.equal(universityJoin.get("univId"), universityId));
            }

            // Lọc theo ngày upload
            if (uploadDate != null) {
                predicates.add(cb.equal(cb.function("DATE", LocalDateTime.class, root.get("uploadedDate")), uploadDate));
            }

            // Nếu là admin, lọc theo status
            if (isAdmin && status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Sắp xếp theo lượt xem hoặc lượt like
            if (sortByViews) {
                query.orderBy(cb.desc(root.get("viewCount"))); // Sắp xếp giảm dần theo lượt xem
            } else if (sortByLikes) {
                query.groupBy(root.get("documentId")); // Gom nhóm theo tài liệu
                query.orderBy(cb.desc(cb.count(reactionJoin.get("isLike")))); // Sắp xếp giảm dần theo số lượt like
            } else if (sortByLikes && sortByViews) {
                query.groupBy(root.get("documentId")); // Gom nhóm theo tài liệu
                query.orderBy(cb.desc(cb.count(reactionJoin.get("isLike"))), cb.desc(root.get("viewCount"))); // Sắp xếp giảm dần theo số lượt like và lượt xem
            } else if (isAdmin && sortByDisLike) {
                query.groupBy(root.get("documentId")); // Gom nhóm theo tài liệu
                query.orderBy(cb.desc(cb.count(reactionJoin.get("isDisLike")))); // Sắp xếp giảm dần theo số lượt dislike
            }
            else {
                query.orderBy(cb.desc(root.get("uploadedDate"))); // Sắp xếp giảm dần theo ngày upload
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<DocumentEntity> relatedDocuments(UUID documentId) {
        return (root, query, criteriaBuilder) -> {
            // JOIN với CourseEntity
            Join<DocumentEntity, Object> courseJoin = root.join("course", JoinType.INNER);

            // JOIN với UniversityEntity
            Join<Object, Object> universityJoin = root.join("university", JoinType.INNER);

            // Lấy tài liệu gốc (tài liệu người dùng đang xem)
            Subquery<DocumentEntity> subquery = query.subquery(DocumentEntity.class);
            Root<DocumentEntity> subRoot = subquery.from(DocumentEntity.class);
            subquery.select(subRoot)
                    .where(criteriaBuilder.equal(subRoot.get("documentId"), documentId));

            // Điều kiện: cùng khóa học hoặc cùng trường hoặc có tiêu đề/mô tả tương tự
            Predicate sameCourse = criteriaBuilder.equal(root.get("course"), subRoot.get("course"));
            Predicate sameUniversity = criteriaBuilder.equal(universityJoin.get("univId"), subRoot.get("university").get("univId"));

            Predicate similarTitle = criteriaBuilder.like(root.get("title"), "%" + subRoot.get("title") + "%");
            Predicate similarDescription = criteriaBuilder.like(root.get("description"), "%" + subRoot.get("description") + "%");

            // Trả về danh sách tài liệu liên quan, nhưng bỏ qua chính tài liệu đó
            return criteriaBuilder.and(
                    criteriaBuilder.notEqual(root.get("documentId"), documentId),
                    criteriaBuilder.or(sameCourse, sameUniversity, similarTitle, similarDescription)
            );
        };
    }

    public static Specification<DocumentEntity> recommendDocuments(List<UUID> courseIds, List<UUID> viewedDocumentIds) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Nếu người dùng theo dõi khóa học, lấy tài liệu của các khóa học đó
            if (courseIds != null && !courseIds.isEmpty()) {
                predicate = criteriaBuilder.or(predicate, root.get("course").get("courseId").in(courseIds));
            }

            // Nếu người dùng không theo dõi khóa học, lấy tài liệu từ những tài liệu họ đã xem trước đó
            if (viewedDocumentIds != null && !viewedDocumentIds.isEmpty()) {
                predicate = criteriaBuilder.or(predicate, root.get("documentId").in(viewedDocumentIds));
            }

            return predicate;
        };
    }
}
