package com.group.docorofile.models.mappers;

import com.group.docorofile.models.dto.AdminDocumentDTO;
import com.group.docorofile.models.dto.CommentDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.utils.FilePreviewUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DocumentMapper {

    private static String uploadDir;

    @Value("${document.upload.dir}")
    public void setUploadDir(String dir) {
        DocumentMapper.uploadDir = dir;
    }

    // Chuyển đổi cho User
    public static UserDocumentDTO toUserDTO(DocumentEntity document) {
        UserDocumentDTO userDocumentDTO = new UserDocumentDTO();
        userDocumentDTO.setDocumentId(document.getDocumentId());
        userDocumentDTO.setTitle(document.getTitle());
        userDocumentDTO.setDescription(document.getDescription());
        userDocumentDTO.setUploadedDate(document.getUploadedDate());
        userDocumentDTO.setViewCount(document.getViewCount());
        userDocumentDTO.setAuthorName(document.getAuthor().getFullName());
        userDocumentDTO.setMemberShip(document.getAuthor().getMembership().getLevel().toString());
        userDocumentDTO.setDownloadCount(document.getAuthor().getDownloadLimit());
        userDocumentDTO.setCourseName(document.getCourse() != null ? document.getCourse().getCourseName() : null);
        userDocumentDTO.setUniversityName(document.getCourse() != null ? document.getCourse().getUniversity().getUnivName() : null);
        userDocumentDTO.setComments(document.getComments().stream().map(CommentDTO::fromEntity).toList());

        String filePathStr = document.getFileUrl();
        String relativePath = filePathStr.replaceFirst("^.*?/uploads/documents/", "");
        String preview = FilePreviewUtils.getCoverImageBase64(uploadDir, relativePath);
        userDocumentDTO.setCoverImageUrl(preview);

        userDocumentDTO.setFileUrl(document.getFileUrl());

        Path path = Paths.get(uploadDir, relativePath);
        String readableSize;
        try {
            long bytes = Files.size(path);
            readableSize = (bytes / 1024) + " KB";
        } catch (IOException e) {
            readableSize = "Unknown";
        }
        userDocumentDTO.setFileSize(readableSize);

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
        adminDocumentDTO.setAuthorName(document.getAuthor().getFullName());
        adminDocumentDTO.setCourseName(document.getCourse() != null ? document.getCourse().getCourseName() : null);
        adminDocumentDTO.setUniversityName(document.getCourse() != null ? document.getCourse().getUniversity().getUnivName() : null);
        adminDocumentDTO.setComments(document.getComments().stream().map(CommentDTO::fromEntity).toList());

        String filePathStr = document.getFileUrl();
        String relativePath = filePathStr.replaceFirst("^.*?/uploads/documents/", "");
        String preview = FilePreviewUtils.getCoverImageBase64(uploadDir, relativePath);
        adminDocumentDTO.setCoverImageUrl(preview); // Lấy trang bìa từ fileUrl

        adminDocumentDTO.setFileUrl(document.getFileUrl());

        Path path = Paths.get(uploadDir, relativePath);
        String readableSize;
        try {
            long bytes = Files.size(path);
            readableSize = (bytes / 1024) + " KB";
        } catch (IOException e) {
            readableSize = "Unknown";
        }
        adminDocumentDTO.setFileSize(readableSize);

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
