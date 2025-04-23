package com.group.docorofile.models.users;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Data
public class CreateUserRequest {

    @NotBlank(message = "userType không được để trống")
    private String userType;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
    private String password;

    // Member-only
    @Min(value = 0, message = "downloadLimit không được âm")
    private Integer downloadLimit;

    private UUID universityId;

    private Boolean isChat;
    private Boolean isComment;
    private Map<String, String> myProfile = new HashMap<>();

    // Moderator-only
    private Boolean isReportManage;
    private Boolean isChatManage;
}
