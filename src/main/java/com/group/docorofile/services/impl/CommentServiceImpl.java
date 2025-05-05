package com.group.docorofile.services.impl;

import com.group.docorofile.entities.CommentEntity;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.models.dto.CommentDTO;
import com.group.docorofile.repositories.CommentRepository;
import com.group.docorofile.repositories.DocumentRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.services.iCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements iCommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository memberRepository;

    // Thêm bình luận
    @Override
    public String addComment(UUID memberId, UUID documentId, String content, UUID parentCommentId) {
        // Kiểm tra thành viên có quyền bình luận không
        MemberEntity member = (MemberEntity) memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        if (!member.isComment()) {
            throw new AccessDeniedException("Bạn không có quyền bình luận!");
        }

        // Kiểm tra tài liệu có tồn tại không
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));

        // Nếu là reply, kiểm tra parent comment có tồn tại không
        CommentEntity parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Bình luận gốc không tồn tại!"));
        }

        // Tạo bình luận mới
        CommentEntity comment = CommentEntity.builder()
                .author(member)
                .document(document)
                .content(content)
                .createdOn(LocalDateTime.now())
                .parentComment(parentComment) // Nếu có parentComment thì lưu, không thì null
                .build();

        commentRepository.save(comment);
        return "Bình luận đã được thêm thành công!";
    }

    // Xóa bình luận
    @Override
    public String deleteComment(UUID commentId, UUID memberId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại!"));

        // Kiểm tra quyền xóa bình luận (chỉ admin hoặc chính chủ mới có thể xóa)
        if (!comment.getAuthor().getUserId().equals(memberId)) {
            return "Bạn không có quyền xóa bình luận này!";
        }

        commentRepository.delete(comment);
        return "Bình luận đã được xóa!";
    }

    @Override
    public List<CommentDTO> getCommentsByDocumentTree(UUID documentId) {
        // Lấy tất cả bình luận của tài liệu
        List<CommentEntity> allComments = commentRepository.findCommentEntitiesByDocument_DocumentId(documentId);

        // Tạo danh sách bình luận cha (Cấp 1)
        List<CommentDTO> parentComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null) // Chỉ lấy bình luận không có cha
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());

        // Ánh xạ bình luận theo ID để xử lý nhanh hơn
        Map<UUID, CommentDTO> commentMap = parentComments.stream()
                .collect(Collectors.toMap(CommentDTO::getCommentId, comment -> comment));

        // Xử lý bình luận cấp 2 (bao gồm cả cấp 3 trở đi, gom vào cấp 2)
        for (CommentEntity comment : allComments) {
            if (comment.getParentComment() != null) {
                UUID parentId = comment.getParentComment().getCommentId();
                CommentDTO parentDTO = commentMap.get(parentId);
                if (parentDTO != null) {
                    parentDTO.getReplies().add(CommentDTO.fromEntity(comment));
                }
            }
        }

        return parentComments;
    }
}
