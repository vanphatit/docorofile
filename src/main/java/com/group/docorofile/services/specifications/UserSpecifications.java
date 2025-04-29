package com.group.docorofile.services.specifications;

import com.group.docorofile.entities.*;
import com.group.docorofile.enums.EMembershipLevel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<UserEntity> withFilters(String search, String role, String plan, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // === 1. Fulltext Search: Name OR Email ===
            if (search != null && !search.isBlank()) {
                Predicate namePredicate = cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%");
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%");
                predicates.add(cb.or(namePredicate, emailPredicate));
            }

            // === 2. Role Filter via entity type ===
            if (role != null && !role.isBlank()) {
                Class<? extends UserEntity> entityClass = switch (role.toLowerCase()) {
                    case "admin" -> AdminEntity.class;
                    case "moderator" -> ModeratorEntity.class;
                    case "member" -> MemberEntity.class;
                    default -> null;
                };

                if (entityClass != null) {
                    predicates.add(cb.equal(root.type(), entityClass));
                }
            }

            // === 3. Plan Filter (Only for Members) ===
            if (plan != null && !plan.isBlank()) {
                try {
                    Join<MemberEntity, MembershipEntity> membershipJoin =
                            cb.treat(root, MemberEntity.class).join("membership", JoinType.LEFT);

                    predicates.add(cb.equal(membershipJoin.get("level"), EMembershipLevel.valueOf(plan)));
                } catch (IllegalArgumentException e) {
                    // Invalid plan passed, ignore or log
                }
            }

            // === 4. Status Filter ===
            if (status != null && !status.isBlank()) {
                if (status.equalsIgnoreCase("active")) {
                    predicates.add(cb.isTrue(root.get("isActive")));
                } else if (status.equalsIgnoreCase("inactive")) {
                    predicates.add(cb.isFalse(root.get("isActive")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}