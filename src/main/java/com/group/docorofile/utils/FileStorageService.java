package com.group.docorofile.utils;

import com.group.docorofile.response.BadRequestError;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.List;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

import static org.apache.commons.io.FilenameUtils.getExtension;

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
        if (file == null || file.isEmpty()) {
            throw new IOException("File rỗng hoặc null");
        }

        String originalExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String filenameWithoutExt = UUID.randomUUID().toString();
        String pdfFilename = filenameWithoutExt + ".pdf";
        Path targetPath = Paths.get(uploadDir, pdfFilename);

        Files.createDirectories(targetPath.getParent());
        Path tempPath = Files.createTempFile(filenameWithoutExt, "." + originalExtension);
        file.transferTo(tempPath.toFile());

        // Chuyển sang PDF nếu không phải PDF
        if (!originalExtension.equalsIgnoreCase("pdf")) {
            File converted = convertToPdf(tempPath.toFile());
            Files.copy(converted.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            converted.delete();
        } else {
            Files.copy(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        tempPath.toFile().delete(); // cleanup file tạm
        return accessUrl + pdfFilename;
    }

    public File convertToPdf(File inputFile) {
        String ext = getExtension(inputFile.getName()).toLowerCase();
        File pdfOutput = new File(inputFile.getParent(), inputFile.getName().replaceFirst("[.][^.]+$", "") + ".pdf");

        try {
            switch (ext) {
                case "docx":
                    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputFile);
                    try (OutputStream os = new FileOutputStream(pdfOutput)) {
                        Docx4J.toPDF(wordMLPackage, os);
                    }
                    break;

                case "pptx":
                    XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(inputFile));
                    Dimension pgsize = ppt.getPageSize();
                    List<XSLFSlide> slides = ppt.getSlides();
                    try (PDDocument pdfDoc = new PDDocument()) {
                        for (XSLFSlide slide : slides) {
                            BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                            Graphics2D g = img.createGraphics();
                            g.setPaint(Color.white);
                            g.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
                            slide.draw(g);
                            g.dispose();

                            PDPage page = new PDPage(new PDRectangle(pgsize.width, pgsize.height));
                            pdfDoc.addPage(page);

                            PDImageXObject pdImage = LosslessFactory.createFromImage(pdfDoc, img);
                            try (PDPageContentStream content = new PDPageContentStream(pdfDoc, page)) {
                                content.drawImage(pdImage, 0, 0);
                            }
                        }
                        pdfDoc.save(pdfOutput);
                    }
                    break;

                default:
                    throw new UnsupportedOperationException("Không hỗ trợ định dạng: " + ext);
            }

        } catch (Exception e) {
            throw new BadRequestError( "Không thể chuyển đổi file sang PDF: " + e.getMessage());
        }

        return pdfOutput;
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
