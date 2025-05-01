package com.group.docorofile.services;

import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.models.dto.UserDocumentDTO;

import java.util.List;
import java.util.UUID;

public interface iFavoriteListService {
    // Thêm tài liệu vào danh sách yêu thích
    String addToFavorites(UUID memberId, UUID documentId);

    // Xóa tài liệu khỏi danh sách yêu thích
    String removeFromFavorites(UUID memberId, UUID documentId);

    // Lấy danh sách tài liệu yêu thích của user
    List<UserDocumentDTO> getFavorites(UUID memberId);

    // Kiểm tra tài liệu có trong danh sách yêu thích không
    boolean isFavorited(UUID memberId, UUID documentId);
}
