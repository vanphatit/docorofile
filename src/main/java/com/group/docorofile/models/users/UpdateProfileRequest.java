package com.group.docorofile.models.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "firstname không được để trống")
    private String fullname;

    @NotNull(message = "universityId không được để trống")
    private UUID universityId;
}
