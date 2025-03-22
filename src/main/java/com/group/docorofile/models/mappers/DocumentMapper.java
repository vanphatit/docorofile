package com.group.docorofile.models.mappers;

import com.group.docorofile.models.dto.AdminDocumentDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.utils.FilePreviewUtils;
import org.springframework.beans.factory.annotation.Value;

public class DocumentMapper {

    @Value("${document.upload.dir}") // Lấy từ application.properties
    private static String uploadDir;

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
        userDocumentDTO.setUniversity(document.getCourse().getUniversity());
        userDocumentDTO.setComments(document.getComments());

        System.out.println("🧾 fileUrl = " + document.getFileUrl());
        String preview = FilePreviewUtils.getCoverImageBase64(uploadDir, document.getFileUrl());
        System.out.println("🖼️ preview result = " + preview);
        userDocumentDTO.setCoverImageUrl(preview); // Lấy trang bìa từ fileUrl

        userDocumentDTO.setLikes((int) document.getReactions().stream().filter(reaction -> reaction.isLike()).count());
        userDocumentDTO.setDislikes((int) document.getReactions().stream().filter(reaction -> reaction.isDislike()).count());
        userDocumentDTO.setViews(document.getViewCount());
        userDocumentDTO.setFavorites(document.getFavoritedBy().size());
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
        String preview = FilePreviewUtils.getCoverImageBase64(uploadDir, document.getFileUrl());
        adminDocumentDTO.setCoverImageUrl(preview); // Lấy trang bìa từ fileUrl
        adminDocumentDTO.setLikes((int) document.getReactions().stream().filter(reaction -> reaction.isLike()).count());
        adminDocumentDTO.setDislikes((int) document.getReactions().stream().filter(reaction -> reaction.isDislike()).count());
        adminDocumentDTO.setViews(document.getViewCount());
        adminDocumentDTO.setFavorites(document.getFavoritedBy().size());
        adminDocumentDTO.setFileUrl(document.getFileUrl());
        adminDocumentDTO.setStatus(document.getStatus());
        adminDocumentDTO.setModifiedOn(document.getModifiedOn());
        adminDocumentDTO.setReportCount(document.getReportCount());
        return adminDocumentDTO;
    }
}
