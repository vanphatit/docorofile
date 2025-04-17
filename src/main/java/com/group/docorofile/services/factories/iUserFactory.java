package com.group.docorofile.services.factories;

import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;

public interface iUserFactory {
    boolean supports(String userType);
    UserEntity createUser(CreateUserRequest request);
}