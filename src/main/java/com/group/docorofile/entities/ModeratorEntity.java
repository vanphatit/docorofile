package com.group.docorofile.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "moderators")
public class ModeratorEntity extends UserEntity {
    private boolean isReportManage;
    private boolean isChatManage;
}