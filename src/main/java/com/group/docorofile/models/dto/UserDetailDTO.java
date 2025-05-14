package com.group.docorofile.models.dto;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.entities.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailDTO {
    private String id;
    private String email;
    private String name;
    private String avatar;
    private String role;
    private String current_plan;
    private String university;
    private int downloadLimit;
    private boolean isChat;
    private boolean isComment;
    private boolean isReportManager;
    private int bookHasCount;
    private int reportedHasCount;
    private int reportCount;
    private LocalDateTime membershipStartDate;
    private LocalDateTime membershipEndDate;
    private int membershipContain;
    private String status;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    public static UserDetailDTO mapTo(UserEntity user) {
        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(user.getUserId().toString());
        dto.setEmail(user.getEmail());
        dto.setName(user.getFullName());
        dto.setAvatar("");
        dto.setStatus(user.isActive() ? "Active" : "Inactive");
        dto.setCreatedOn(user.getCreatedOn());
        dto.setModifiedOn(user.getModifiedOn());

        if (user instanceof MemberEntity member) {
            dto.setRole("Member");
            if (member.getMembership() != null) {
                dto.setCurrent_plan(member.getMembership().getLevel().name());
                if (member.getMembership().getStartDate() != null && member.getMembership().getEndDate() != null) {
                    int containDays = (int) ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            member.getMembership().getEndDate()
                    );
                    dto.setMembershipContain(containDays);
                }
                dto.setMembershipStartDate(member.getMembership().getStartDate());
                dto.setMembershipEndDate(member.getMembership().getEndDate());
            }
            if (member.getStudyAt() != null)
                dto.setUniversity(member.getStudyAt().getUnivName());
            dto.setDownloadLimit(member.getDownloadLimit());
            dto.setChat(member.isChat());
            dto.setComment(member.isComment());
            dto.setBookHasCount(0);
            dto.setReportedHasCount(0);
            dto.setReportCount(0);
        } else if (user instanceof ModeratorEntity moderator) {
            dto.setRole("Moderator");
            dto.setReportManager(moderator.isReportManage());
        } else {
            dto.setRole("Admin");
        }
        return dto;
    }
}
