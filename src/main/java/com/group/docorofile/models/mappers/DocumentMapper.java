package com.group.docorofile.models.mappers;

import com.group.docorofile.models.dto.AdminDocumentDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.utils.PDFUtils;
import org.springframework.context.annotation.Bean;

public class DocumentMapper {

    // Chuyển đổi cho User
    public static UserDocumentDTO toUserDTO(DocumentEntity document) {
        UserDocumentDTO userDocumentDTO = new UserDocumentDTO();
        userDocumentDTO.setDocumentId(document.getDocumentId());
        userDocumentDTO.setTitle(document.getTitle());
        userDocumentDTO.setDescription(document.getDescription());
        userDocumentDTO.setUploadedDate(document.getUploadedDate());
        userDocumentDTO.setViewCount(document.getViewCount());
        userDocumentDTO.setAuthor(document.getAuthor());
        userDocumentDTO.setCourse(document.getCourse());
        userDocumentDTO.setComments(document.getComments());
        userDocumentDTO.setCoverImageUrl(PDFUtils.getCoverImageFromPDF(document.getFileUrl())); // Lấy trang bìa từ fileUrl
        return userDocumentDTO;
    }

    // Chuyển đổi cho Admin
    public static AdminDocumentDTO toAdminDTO(DocumentEntity document) {
        AdminDocumentDTO adminDocumentDTO = new AdminDocumentDTO();
        adminDocumentDTO.setDocumentId(document.getDocumentId());
        adminDocumentDTO.setTitle(document.getTitle());
        adminDocumentDTO.setDescription(document.getDescription());
        adminDocumentDTO.setUploadedDate(document.getUploadedDate());
        adminDocumentDTO.setViewCount(document.getViewCount());
        adminDocumentDTO.setAuthor(document.getAuthor());
        adminDocumentDTO.setCourse(document.getCourse());
        adminDocumentDTO.setComments(document.getComments());
        adminDocumentDTO.setCoverImageUrl(PDFUtils.getCoverImageFromPDF(document.getFileUrl())); // Lấy trang bìa từ fileUrl
        adminDocumentDTO.setFileUrl(document.getFileUrl());
        adminDocumentDTO.setStatus(document.getStatus());
        adminDocumentDTO.setModifiedOn(document.getModifiedOn());
        adminDocumentDTO.setReportCount(document.getReportCount());
        return adminDocumentDTO;
    }
}
