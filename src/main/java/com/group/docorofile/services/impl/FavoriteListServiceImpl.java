package com.group.docorofile.services.impl;

import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.entities.FavoriteListEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.models.mappers.DocumentMapper;
import com.group.docorofile.repositories.DocumentRepository;
import com.group.docorofile.repositories.FavoriteListRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.services.iFavoriteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteListServiceImpl implements iFavoriteListService {
    @Autowired
    private FavoriteListRepository favoriteListRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository memberRepository;

    // Thêm tài liệu vào danh sách yêu thích
    @Override
    public String addToFavorites(UUID memberId, UUID documentId) {
        if (favoriteListRepository.existsByMember_UserIdAndDocument_DocumentId(memberId, documentId)) {
            return "Tài liệu đã có trong danh sách yêu thích.";
        }

        MemberEntity member = (MemberEntity) memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));

        FavoriteListEntity favorite = FavoriteListEntity.builder()
                .member(member)
                .document(document)
                .build();

        favoriteListRepository.save(favorite);
        return "Đã thêm vào danh sách yêu thích.";
    }

    // Xóa tài liệu khỏi danh sách yêu thích
    @Override
    public String removeFromFavorites(UUID memberId, UUID documentId) {
        List<FavoriteListEntity> favoriteList = favoriteListRepository.findByMember_UserId(memberId);
        for (FavoriteListEntity favorite : favoriteList) {
            if (favorite.getDocument().getDocumentId().equals(documentId)) {
                favoriteListRepository.delete(favorite);
                return "Đã xóa khỏi danh sách yêu thích.";
            }
        }
        return "Tài liệu không có trong danh sách yêu thích.";
    }

    // Lấy danh sách tài liệu yêu thích của user
    @Override
    public List<UserDocumentDTO> getFavorites(UUID memberId) {
        // Kiểm tra xem thành viên có tồn tại không
        MemberEntity member = (MemberEntity) memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundError("Người dùng không tồn tại!"));

        // Lấy danh sách yêu thích của người dùng
        List<FavoriteListEntity> favoriteList = favoriteListRepository.findByMember_UserId(memberId);

        // Kiểm tra nếu danh sách rỗng
        if (favoriteList.isEmpty()) {
            throw new NotFoundError("Danh sách yêu thích trống!");
        }

        // Chuyển đổi sang UserDocumentDTO
        return favoriteList.stream()
                .map(favorite -> DocumentMapper
                        .toUserDTO(favorite.getDocument()))
                .toList();
    }

    // Kiểm tra tài liệu có trong danh sách yêu thích không
    @Override
    public boolean isFavorited(UUID memberId, UUID documentId) {
        return favoriteListRepository.existsByMember_UserIdAndDocument_DocumentId(memberId, documentId);
    }
}
