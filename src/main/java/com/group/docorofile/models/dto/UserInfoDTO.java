package com.group.docorofile.models.dto;

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
    private String phone;
    private String address;
    private String avatar;
    private String role;
    private String membershipName;
}
