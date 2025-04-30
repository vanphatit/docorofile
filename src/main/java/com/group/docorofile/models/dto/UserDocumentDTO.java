package com.group.docorofile.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDocumentDTO extends DocumentDTO {
    private int downloadCount;
}


