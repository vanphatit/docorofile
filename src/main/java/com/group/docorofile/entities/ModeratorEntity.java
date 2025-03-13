package com.group.docorofile.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "moderators")
public class ModeratorEntity extends UserEntity {
    private boolean isReportManage;
    private boolean isChatManage;
}