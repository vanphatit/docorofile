package com.group.docorofile.services.strategies;

import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.models.users.UpdateUserRequest;

public interface iUserUpdateStrategy {
    boolean supports(UserEntity user);
    void update(UserEntity user, UpdateUserRequest request);

    // Optional: default empty, override only where needed
    default void updateProfile(UserEntity user, UpdateProfileRequest request) {
        throw new UnsupportedOperationException("This strategy does not support profile update.");
    }
}
