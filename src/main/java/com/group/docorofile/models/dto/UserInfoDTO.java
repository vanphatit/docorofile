package com.group.docorofile.models.dto;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String id;
    private String email;
    private String name;
    private String avatar;
    private String role;
    private String current_plan;
    private String status;

    public UserInfoDTO mapToUserInfo(UserEntity user) {
        this.id = user.getUserId().toString();
        this.email = user.getEmail();
        this.name = user.getFullName();
        this.avatar ="";
        this.role = user.getClass().getSimpleName();
        this.status = user.isActive() ? "Active" : "Inactive";

        if (user instanceof MemberEntity member) {
            this.role = "Member";
            this.current_plan = member.getMembership().getLevel().name();
        } else {
            this.current_plan = null;
        }

        return this;
    }
}
