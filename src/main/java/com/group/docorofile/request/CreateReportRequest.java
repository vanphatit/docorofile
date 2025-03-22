package com.group.docorofile.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateReportRequest {
    private UUID reportedDocId;
    private UUID reporterId;
    private String detail;
}
