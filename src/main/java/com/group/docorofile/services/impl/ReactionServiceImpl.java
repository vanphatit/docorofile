package com.group.docorofile.services.impl;

import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ReactionEntity;
import com.group.docorofile.repositories.DocumentRepository;
import com.group.docorofile.repositories.MemberRepository;
import com.group.docorofile.repositories.ReactionRepository;
import com.group.docorofile.services.iReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReactionServiceImpl implements iReactionRepository {
    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public String updateReact(UUID userId, UUID documentId, boolean isLike, boolean isDislike) {

        if (isLike && isDislike) {
            throw new IllegalArgumentException("Không thể vừa like vừa dislike!");
        }

        // Kiểm tra tài liệu có tồn tại không
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));

        // Kiểm tra người dùng có tồn tại không
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Tìm phản ứng của người dùng trên tài liệu
        Optional<ReactionEntity> existingReaction = reactionRepository.findByUserAndDocument(userId, documentId);

        if (existingReaction.isPresent()) {
            // Nếu phản ứng đã tồn tại, cập nhật giá trị like/dislike
            ReactionEntity reaction = existingReaction.get();
            reaction.setLike(isLike);
            reaction.setDislike(isDislike);
            reaction.setModifiedOn(LocalDateTime.now());
            reactionRepository.save(reaction);
            return "Phản ứng đã được cập nhật!";
        } else {
            // Nếu chưa có phản ứng, tạo mới
            ReactionEntity newReaction = ReactionEntity.builder()
                    .author(member)
                    .document(document)
                    .isLike(isLike)
                    .isDislike(isDislike)
                    .createdOn(LocalDateTime.now())
                    .build();
            reactionRepository.save(newReaction);
            return "Phản ứng mới đã được thêm!";
        }
    }

    @Override
    public int countLike(UUID documentId) {
        return reactionRepository.countLikeByDocumentId(documentId);
    }

    @Override
    public int countDislike(UUID documentId) {
        return reactionRepository.countDislikeByDocumentId(documentId);
    }
}
