package com.group.docorofile.services;

import java.util.UUID;

public interface iReactionRepository {
    String updateReact(UUID userId, UUID documentId, boolean isLike, boolean isDislike);

    int countLike(UUID documentId);

    int countDislike(UUID documentId);

    String getUserReaction(UUID userId, UUID documentId);
}
