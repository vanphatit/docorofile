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
}
