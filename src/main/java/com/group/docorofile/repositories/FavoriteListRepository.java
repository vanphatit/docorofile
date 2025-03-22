package com.group.docorofile.repositories;

import com.group.docorofile.entities.FavoriteListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteListRepository extends JpaRepository<FavoriteListEntity, UUID> {
    List<FavoriteListEntity> findByMember_UserId(UUID memberId); // Lấy danh sách yêu thích của user
    boolean existsByMember_UserIdAndDocument_DocumentId(UUID memberId, UUID documentId); // Kiểm tra xem user đã thích tài liệu chưa
}
