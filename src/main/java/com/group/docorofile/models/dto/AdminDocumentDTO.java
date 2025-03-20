package com.group.docorofile.models.dto;

import com.group.docorofile.enums.EDocumentStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDocumentDTO extends DocumentDTO {
    private String fileUrl;
    private EDocumentStatus status;
    private LocalDateTime modifiedOn;
    private int reportCount;
}
