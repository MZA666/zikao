package com.example.utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readFileContent(String filePath) {
        try {
            if (filePath.toLowerCase().endsWith(".doc")) {
                return readDocFile(filePath);
            } else if (filePath.toLowerCase().endsWith(".docx")) {
                return readDocxFile(filePath);
            } else if (filePath.toLowerCase().endsWith(".txt")) {
                return readTxtFile(filePath);
            } else {
                throw new IllegalArgumentException("不支持的文件格式: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 读取文件字节数组
     * @param filePath 文件路径
     * @return 文件字节数组
     */
    public static byte[] readBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    /**
     * 读取.doc文件内容
     */
    private static String readDocFile(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath);
             HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            return String.join("\n", extractor.getParagraphText());
        }
    }

    /**
     * 读取.docx文件内容
     */
    private static String readDocxFile(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 读取.txt文件内容
     */
    private static String readTxtFile(String filePath) throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        return java.nio.file.Files.readString(path);
    }
}