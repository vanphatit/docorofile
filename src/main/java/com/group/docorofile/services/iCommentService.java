package com.group.docorofile.services;

import java.util.UUID;

public interface iCommentService {
    // Thêm bình luận
    String addComment(UUID memberId, UUID documentId, String content, UUID parentCommentId);

    // Xóa bình luận
    String deleteComment(UUID commentId, UUID memberId);
}
