package com.example.utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.*;

public class WordToHtmlUtil {

    /**
     * 将Word文档转换为HTML
     * @param filePath Word文件路径
     * @return HTML字符串
     * @throws Exception
     */
    public static String convertWordToHtml(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }

        String extension = getFileExtension(filePath).toLowerCase();
        if (extension.equals(".doc")) {
            return convertDocToHtml(filePath);
        } else if (extension.equals(".docx")) {
            return convertDocxToHtml(filePath);
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 转换.doc文件为HTML - 使用WordExtractor提取文本
     */
    private static String convertDocToHtml(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            
            String[] paragraphs = extractor.getParagraphText();
            
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Word Document</title>");
            sb.append("<style>");
            sb.append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; }");
            sb.append("p { margin: 10px 0; }");
            sb.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }");
            sb.append("td, th { border: 1px solid #000; padding: 8px; }");
            sb.append("</style></head><body>");
            
            for (String paragraph : paragraphs) {
                if (paragraph != null) {
                    // 过滤掉可能的控制字符
                    paragraph = paragraph.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F]", "");
                    if (paragraph.trim().length() > 0) {
                        sb.append("<p>").append(escapeHtml(paragraph)).append("</p>");
                    }
                }
            }
            
            sb.append("</body></html>");
            
            extractor.close();
            document.close();
            
            return sb.toString();
        }
    }

    /**
     * 转换.docx文件为HTML
     */
    private static String convertDocxToHtml(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Word Document</title>");
            sb.append("<style>");
            sb.append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; }");
            sb.append("p { margin: 10px 0; }");
            sb.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }");
            sb.append("td, th { border: 1px solid #000; padding: 8px; }");
            sb.append("</style></head><body>");
            
            // 添加段落内容
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isEmpty()) {
                    sb.append("<p>").append(escapeHtml(text)).append("</p>");
                }
            }
            
            // 添加表格内容
            for (XWPFTable table : document.getTables()) {
                sb.append("<table>");
                for (XWPFTableRow row : table.getRows()) {
                    sb.append("<tr>");
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        String text = cell.getText();
                        sb.append("<td>")
                          .append(text != null ? escapeHtml(text) : "")
                          .append("</td>");
                    }
                    sb.append("</tr>");
                }
                sb.append("</table>");
            }
            
            sb.append("</body></html>");
            
            return sb.toString();
        }
    }

    /**
     * 转义HTML特殊字符
     */
    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;")
                  .replace("\n", "<br/>");
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
}