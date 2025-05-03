package com.group.docorofile.models.university;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UniversityTagDTO {
    private UUID univId;
    private String univName;
}

