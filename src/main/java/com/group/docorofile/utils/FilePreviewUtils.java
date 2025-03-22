package com.group.docorofile.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class FilePreviewUtils {

    public static String getCoverImageBase64(String uploadDir, String fileName) {
        try {
            File file;

            if (fileName.contains(":") || fileName.startsWith("/")) {
                // Là đường dẫn tuyệt đối
                file = new File(fileName);
            } else {
                // Là tên file tương đối → dùng uploadDir
                file = Paths.get(uploadDir, fileName).toFile();
            }

            String ext = getExtension(fileName).toLowerCase();
            BufferedImage image;

            switch (ext) {
                case "pdf":
                    image = renderPdf(file);
                    break;
                case "docx":
                    image = renderDocx(file);
                    break;
                case "pptx":
                    image = renderPptx(file);
                    break;
                default:
                    return null; // unsupported
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes); // convert to base64

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage renderPdf(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage image = renderer.renderImageWithDPI(0, 150, ImageType.RGB);
        doc.close();
        return image;
    }

    private static BufferedImage renderDocx(File file) throws IOException {
//        XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
//        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
//        String firstLine = extractor.getText().split("\n")[0]; // get first line
//        extractor.close();
//        return renderTextToImage(firstLine);
        XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
        List<XWPFParagraph> paragraphs = doc.getParagraphs();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(paragraphs.size(), 3); i++) {
            sb.append(paragraphs.get(i).getText()).append("\n");
        }

        return renderTextToImage(sb.toString(), 20); // font size 20
    }

    private static BufferedImage renderPptx(File file) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file));
        XSLFSlide slide = ppt.getSlides().get(0);
        Dimension size = ppt.getPageSize();

        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(Color.white);
        g.fill(new Rectangle2D.Float(0, 0, size.width, size.height));
        slide.draw(g);
        g.dispose();
        return img;
    }

//    private static BufferedImage renderTextToImage(String text) {
//        BufferedImage img = new BufferedImage(600, 100, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = img.createGraphics();
//        g.setPaint(Color.white);
//        g.fillRect(0, 0, 600, 100);
//        g.setPaint(Color.black);
//        g.setFont(new Font("Arial", Font.BOLD, 20));
//        g.drawString(text, 10, 50);
//        g.dispose();
//        return img;
//    }

    private static BufferedImage renderTextToImage(String text, int fontSize) {
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        BufferedImage dummyImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dummyImg.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text) + 40;
        int height = fm.getHeight() * 5 + 40;
        g2d.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = image.createGraphics();

        // Background trắng
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Text đen
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);

        // Anti-aliasing cho nét mịn
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 20;
        int y = 40;
        for (String line : text.split("\n")) {
            g2d.drawString(line, x, y);
            y += fm.getHeight();
        }

        g2d.dispose();
        return image;
    }

    private static String getExtension(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
