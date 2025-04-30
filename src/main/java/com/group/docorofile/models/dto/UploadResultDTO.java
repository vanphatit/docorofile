package com.group.docorofile.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultDTO {
    private List<UserDocumentDTO> documents;
    private int addedTurns;
    private int totalDownloadLimit;
}