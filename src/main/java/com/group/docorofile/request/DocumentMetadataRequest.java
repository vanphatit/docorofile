package com.group.docorofile.request;

import lombok.Data;

import java.util.UUID;

@Data
public class DocumentMetadataRequest {
    private String documentId;
    private String title;
    private String description;
    private String nameCourse;
    private String nameUniversity;


}
