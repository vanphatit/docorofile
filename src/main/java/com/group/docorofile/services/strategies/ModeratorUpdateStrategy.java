package com.group.docorofile.services.strategies;

import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import org.springframework.stereotype.Component;

@Component
public class ModeratorUpdateStrategy implements iUserUpdateStrategy {
    @Override
    public boolean supports(UserEntity user) {
        return user instanceof ModeratorEntity;
    }

    @Override
    public void update(UserEntity user, CreateUserRequest request) {
        ModeratorEntity moderator = (ModeratorEntity) user;
        moderator.setFullName(request.getFullName());
        moderator.setEmail(request.getEmail());
        moderator.setReportManage(Boolean.TRUE.equals(request.getIsReportManage()));
    }
}
