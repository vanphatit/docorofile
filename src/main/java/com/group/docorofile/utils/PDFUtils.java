package com.group.docorofile.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class PDFUtils {

    // Trích xuất trang đầu tiên từ file PDF và chuyển thành ảnh base64
    public static String getCoverImageFromPDF(String fileUrl) {
        try {
            File file = new File(fileUrl);
            if (!file.exists()) {
                return null; // Nếu file không tồn tại, trả về null
            }

            PDDocument document = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150, ImageType.RGB); // Trang đầu tiên

            // Chuyển ảnh sang base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            document.close();

            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
