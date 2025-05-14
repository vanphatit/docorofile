package com.group.docorofile.services.specifications;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.entities.UniversityEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.time.LocalDate;
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

        public static Specification<DocumentEntity> filterDocuments(List<DocumentEntity> searchDocuments, UUID courseId, UUID universityId, LocalDateTime uploadDate,
                                                                    boolean sortByViews, boolean sortByLikes, boolean sortByDisLike,
                                                                    boolean sortByNewest, boolean sortByOldest, boolean sortByReportCount,
                                                                    String status, boolean isAdmin
        ) {
            return (Root<DocumentEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Nếu có tài liệu tìm kiếm, thêm điều kiện vào
                if (searchDocuments != null && !searchDocuments.isEmpty()) {
                    List<Predicate> searchPredicates = new ArrayList<>();
                    for (DocumentEntity document : searchDocuments) {
                        Predicate idPredicate = cb.equal(root.get("documentId"), document.getDocumentId());
                        searchPredicates.add(idPredicate);
                    }
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }

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

                if (uploadDate != null) {
                    Timestamp start = Timestamp.valueOf(uploadDate);
                    Timestamp end = Timestamp.valueOf(uploadDate.plusDays(1));

                    predicates.add(cb.between(root.get("uploadedDate"), start, end));
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
                    query.orderBy(cb.desc(cb.count(reactionJoin.get("isDislike")))); // Sắp xếp giảm dần theo số lượt dislike
                } else if (sortByReportCount) {
                    query.groupBy(root.get("documentId")); // Gom nhóm theo tài liệu
                    query.orderBy(cb.desc(cb.count(root.get("reportCount")))); // Sắp xếp giảm dần theo số lượt report
                } else if (sortByNewest) {
                    query.orderBy(cb.desc(root.get("uploadedDate"))); // Sắp xếp giảm dần theo ngày upload
                } else if (sortByOldest) {
                    query.orderBy(cb.asc(root.get("uploadedDate"))); // Sắp xếp tăng dần theo ngày upload
                }
                else {
                    query.orderBy(cb.desc(root.get("uploadedDate"))); // Sắp xếp giảm dần theo ngày upload
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }

    public static Specification<DocumentEntity> relatedDocuments(DocumentEntity originalDoc) {
        return (root, query, cb) -> {
            // Join document.course
            Join<DocumentEntity, CourseEntity> courseJoin = root.join("course", JoinType.LEFT);

            // Join course.university
            Join<CourseEntity, UniversityEntity> universityJoin = courseJoin.join("university", JoinType.LEFT);

            // Điều kiện
            Predicate notSameDoc = cb.notEqual(root.get("documentId"), originalDoc.getDocumentId());
            Predicate sameCourse = cb.equal(courseJoin.get("courseId"), originalDoc.getCourse().getCourseId());
            Predicate sameUniversity = cb.equal(universityJoin.get("univId"), originalDoc.getCourse().getUniversity().getUnivId());
            Predicate similarTitle = cb.like(cb.lower(root.get("title")), "%" + originalDoc.getTitle().toLowerCase() + "%");
            Predicate similarDesc = cb.like(cb.lower(root.get("description")), "%" + originalDoc.getDescription().toLowerCase() + "%");

            return cb.and(
                    notSameDoc,
                    cb.or(sameCourse, sameUniversity, similarTitle, similarDesc)
            );
        };
    }

    public static Specification<DocumentEntity> recommendDocuments(List<UUID> courseIds, List<UUID> viewedDocumentIds) {
        return (root, query, cb) -> {
            Predicate coursePredicate = null;
            Predicate viewedPredicate = null;

            // Tài liệu thuộc các khóa học đang theo dõi
            if (courseIds != null && !courseIds.isEmpty()) {
                coursePredicate = root.get("course").get("courseId").in(courseIds);
            }

            // Tài liệu đã từng xem
            if (viewedDocumentIds != null && !viewedDocumentIds.isEmpty()) {
                viewedPredicate = root.get("documentId").in(viewedDocumentIds);
            }

            // Gộp logic recommend
            Predicate recommendPredicate;
            if (coursePredicate != null && viewedPredicate != null) {
                recommendPredicate = cb.or(coursePredicate, viewedPredicate);
            } else if (coursePredicate != null) {
                recommendPredicate = coursePredicate;
            } else if (viewedPredicate != null) {
                recommendPredicate = viewedPredicate;
            } else {
                // Không có dữ liệu recommend → không match gì cả
                return cb.disjunction(); // trả về false
            }

            // Lọc trạng thái: khác DRAFT & DELETED
            Predicate validStatus = cb.and(
                    cb.notEqual(root.get("status"), "DRAFT"),
                    cb.notEqual(root.get("status"), "DELETED")
            );

            return cb.and(recommendPredicate, validStatus);
        };
    }
}
