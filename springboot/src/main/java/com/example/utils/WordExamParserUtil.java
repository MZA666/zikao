package com.example.utils;

import com.example.entity.exam.Question;
import com.example.entity.exam.QuestionOption;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word文档解析工具类，用于解析考试题库文档
 */
public class WordExamParserUtil {

    /**
     * 解析Word文档中的考试题目
     * @param filePath Word文件路径
     * @return 解析出的题目列表
     */
    public static List<Question> parseExamQuestions(String filePath) {
        List<Question> questions = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            // 判断文件类型（.doc 或 .docx）
            if (filePath.toLowerCase().endsWith(".doc")) {
                questions = parseDocFile(inputStream);
            } else if (filePath.toLowerCase().endsWith(".docx")) {
                questions = parseDocxFile(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * 解析.doc文件
     */
    private static List<Question> parseDocFile(InputStream inputStream) throws IOException {
        List<Question> questions = new ArrayList<>();
        HWPFDocument document = new HWPFDocument(inputStream);
        WordExtractor extractor = new WordExtractor(document);
        String[] paragraphs = extractor.getParagraphText();
        questions = parseQuestionsFromText(String.join("\n", paragraphs));
        
        extractor.close();
        document.close();
        
        return questions;
    }

    /**
     * 解析.docx文件
     */
    private static List<Question> parseDocxFile(InputStream inputStream) throws IOException {
        List<Question> questions = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            questions = parseQuestionsFromText(content.toString());
        }
        return questions;
    }

    /**
     * 从文本内容中解析题目
     */
    private static List<Question> parseQuestionsFromText(String content) {
        List<Question> questions = new ArrayList<>();
        
        // 按照考题模板解析不同类型的题目
        // 匹配题目编号（1. 2. 3. 或 1、2、3、等格式）
        String questionPattern = "(?m)^\\s*([0-9]+)[.、]\\s*(.*?)(?=\\n\\s*[0-9]+[.、]|\\n答案|$)";
        
        Pattern pattern = Pattern.compile(questionPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String questionText = matcher.group(2).trim();
            
            Question question = new Question();
            // 尝试识别题目类型
            if (isSingleChoiceQuestion(questionText)) {
                question.setType("SINGLE_CHOICE");
                parseSingleChoiceQuestion(question, questionText);
            } else if (isMultipleChoiceQuestion(questionText)) {
                question.setType("MULTIPLE_CHOICE");
                parseMultipleChoiceQuestion(question, questionText);
            } else if (isTrueFalseQuestion(questionText)) {
                question.setType("TRUE_FALSE");
                parseTrueFalseQuestion(question, questionText);
            } else if (isFillBlankQuestion(questionText)) {
                question.setType("FILL_BLANK");
                parseFillBlankQuestion(question, questionText);
            } else {
                question.setType("ESSAY"); // 默认为问答题
                parseEssayQuestion(question, questionText);
            }
            
            questions.add(question);
        }
        
        return questions;
    }

    /**
     * 识别是否为单选题
     */
    private static boolean isSingleChoiceQuestion(String questionText) {
        return questionText.contains("A.") || questionText.contains("A、") ||
               questionText.contains("B.") || questionText.contains("B、") ||
               questionText.contains("C.") || questionText.contains("C、") ||
               questionText.contains("D.") || questionText.contains("D、");
    }

    /**
     * 识别是否为多选题
     */
    private static boolean isMultipleChoiceQuestion(String questionText) {
        // 多选题通常有更多的选项（E、F、G、H等）
        return isSingleChoiceQuestion(questionText) && 
               (questionText.contains("E.") || questionText.contains("E、") ||
                questionText.contains("F.") || questionText.contains("F、") ||
                questionText.contains("G.") || questionText.contains("G、") ||
                questionText.contains("H.") || questionText.contains("H、"));
    }

    /**
     * 识别是否为判断题
     */
    private static boolean isTrueFalseQuestion(String questionText) {
        return questionText.toLowerCase().contains("正确") || 
               questionText.toLowerCase().contains("错误") ||
               questionText.contains("（√）") || 
               questionText.contains("（×）") ||
               questionText.contains("（对）") || 
               questionText.contains("（错）");
    }

    /**
     * 识别是否为填空题
     */
    private static boolean isFillBlankQuestion(String questionText) {
        return questionText.contains("（）") || questionText.contains("()");
    }

    /**
     * 解析单选题
     */
    private static void parseSingleChoiceQuestion(Question question, String questionText) {
        // 提取题干（到第一个选项之前）
        String[] lines = questionText.split("\n");
        StringBuilder content = new StringBuilder();
        List<QuestionOption> options = new ArrayList<>();
        
        boolean inOptions = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("A.") || line.startsWith("A、") ||
                line.startsWith("B.") || line.startsWith("B、") ||
                line.startsWith("C.") || line.startsWith("C、") ||
                line.startsWith("D.") || line.startsWith("D、")) {
                inOptions = true;
                // 解析选项
                String optionKey = line.substring(0, 1); // A, B, C, D
                String optionContent = line.substring(2).trim(); // 去掉选项标识后的部分
                QuestionOption option = new QuestionOption();
                option.setOptionKey(optionKey);
                option.setContent(optionContent);
                options.add(option);
            } else if (line.startsWith("答案：")) {
                // 解析答案
                question.setAnswer(line.substring(3).trim());
            } else if (line.startsWith("解析：")) {
                // 解析解析
                question.setAnalysis(line.substring(3).trim());
            } else if (!inOptions) {
                content.append(line).append(" ");
            }
        }
        
        question.setContent(content.toString().trim());
        question.setOptions(options);
    }

    /**
     * 解析多选题
     */
    private static void parseMultipleChoiceQuestion(Question question, String questionText) {
        // 与单选题类似，但可能有更多选项
        parseSingleChoiceQuestion(question, questionText);
    }

    /**
     * 解析判断题
     */
    private static void parseTrueFalseQuestion(Question question, String questionText) {
        question.setContent(questionText.split("\n")[0].trim());
        // 查找答案
        if (questionText.contains("答案：")) {
            String[] parts = questionText.split("答案：");
            if (parts.length > 1) {
                question.setAnswer(parts[1].split("\n")[0].trim());
            }
        } else if (questionText.contains("（")) {
            // 答案可能直接在题目中
            Pattern answerPattern = Pattern.compile("（([√×对错])）|（(正确|错误)）");
            Matcher matcher = answerPattern.matcher(questionText);
            if (matcher.find()) {
                question.setAnswer(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            }
        }
    }

    /**
     * 解析填空题
     */
    private static void parseFillBlankQuestion(Question question, String questionText) {
        question.setContent(questionText.split("\n")[0].trim());
        // 查找答案
        if (questionText.contains("答案：")) {
            String[] parts = questionText.split("答案：");
            if (parts.length > 1) {
                question.setAnswer(parts[1].split("\n")[0].trim());
            }
        }
    }

    /**
     * 解析问答题
     */
    private static void parseEssayQuestion(Question question, String questionText) {
        question.setContent(questionText.split("\n")[0].trim());
        // 查找答案
        if (questionText.contains("答案：")) {
            String[] parts = questionText.split("答案：");
            if (parts.length > 1) {
                question.setAnswer(parts[1].trim());
            }
        }
    }
}