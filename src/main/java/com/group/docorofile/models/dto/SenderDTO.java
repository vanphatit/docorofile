package com.group.docorofile.models.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderDTO {
    private UUID userId;
    private String fullName;
}
