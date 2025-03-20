package com.group.docorofile.models.users;

import lombok.Data;

import java.util.Map;
import java.util.HashMap;

@Data
public class CreateUserRequest {
    // Xác định loại user: ADMIN, MODERATOR, MEMBER
    private String userType;

    // Các field chung cho UserEntity
    private String fullName;
    private String email;
    private String password;

    // Các field riêng cho MemberEntity
    private Integer downloadLimit;
    private Boolean isChat;
    private Boolean isComment;
    private Map<String, String> myProfile = new HashMap<>();

    // Các field riêng cho ModeratorEntity
    private Boolean isReportManage;
    private Boolean isChatManage;
}