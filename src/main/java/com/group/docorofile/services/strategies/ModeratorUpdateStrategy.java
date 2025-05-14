package com.group.docorofile.services.strategies;

import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.UpdateUserRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ModeratorUpdateStrategy implements iUserUpdateStrategy {
    @Override
    public boolean supports(UserEntity user) {
        return user instanceof ModeratorEntity;
    }

    @Override
    public void update(UserEntity user, UpdateUserRequest request) {
        ModeratorEntity moderator = (ModeratorEntity) user;
        moderator.setFullName(request.getFullName());
        moderator.setReportManage(Objects.equals(request.getIsReportManage(), "True"));
    }
}
