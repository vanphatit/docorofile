package com.group.docorofile.models.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String fullName;

    private String universityName;
    private int downloadLimit;
    private String isChat;
    private String isComment;

    private String isReportManage;

}
