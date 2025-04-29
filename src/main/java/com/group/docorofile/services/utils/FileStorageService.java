package com.group.docorofile.services.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${document.upload.dir}")
    private String uploadDir;

    @Value("${document.access.url}")
    private String accessUrl;

    // Kiểm tra định dạng file hợp lệ
    private static final String[] ALLOWED_FILE_TYPES = {"pdf", "docx", "pptx", "txt", "zip"};

    @PostConstruct
    public void initUploadDir() {
        // Nếu đường dẫn là tương đối, nối với thư mục gốc project thật
        if (!Paths.get(uploadDir).isAbsolute()) {
            String projectDir = System.getProperty("user.dir");
            uploadDir = Paths.get(projectDir, uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .toString();
        }

        // Tạo thư mục nếu chưa có
        try {
            Files.createDirectories(Paths.get(uploadDir));
            System.out.println("Thư mục upload đã được tạo (nếu chưa có): " + uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload: " + uploadDir, e);
        }
    }

    private boolean isValidFileType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (String allowed : ALLOWED_FILE_TYPES) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    // Lưu file và trả về đường dẫn file
    public String saveFile(MultipartFile file) throws IOException {
        if (!isValidFileType(file.getOriginalFilename())) {
            throw new IOException("Định dạng tài liệu không được hỗ trợ!");
        }

        // Tạo tên file mới để tránh trùng
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String filename = UUID.randomUUID() + fileExtension;
        Path targetPath = Paths.get(uploadDir, filename);

        // Tạo thư mục nếu chưa có
        Files.createDirectories(targetPath.getParent());

        // Lưu file vào thư mục
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Lỗi khi lưu tài liệu! Vui lòng thử lại.");
        }

        // Trả về đường dẫn file
        return accessUrl + filename;
    }

    // Lấy file từ đường dẫn
    public Path getFilePath(String fileName) {
        return Paths.get(uploadDir + fileName);
    }

    // Xóa file
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir + fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}
