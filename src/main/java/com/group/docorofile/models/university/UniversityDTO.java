package com.group.docorofile.models.university;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityDTO implements Serializable{
    private UUID univId;
    private String univName;
    private String address;
}
