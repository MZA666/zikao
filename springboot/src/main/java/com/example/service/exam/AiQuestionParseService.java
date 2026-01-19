package com.example.service.exam;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.exam.AiParsedOption;
import com.example.entity.exam.AiParsedQuestion;
import com.example.entity.exam.Question;
import com.example.entity.exam.QuestionOption;
import com.example.utils.FileUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI题目解析服务类
 * 使用大模型API解析文件中的题目内容
 */
@Service
public class AiQuestionParseService {

    /**
     * 解析文件中的题目
     * @param filePath 文件路径
     * @return 解析出的题目列表
     */
    public List<Question> parseQuestionsFromFile(String filePath) {
        // 读取文件内容
        String fileContent = FileUtil.readFileContent(filePath);
        
        // 调用大模型API解析题目
        List<AiParsedQuestion> aiParsedQuestions = callAiModel(fileContent);
        
        // 将AI解析结果转换为系统需要的Question对象
        return convertToQuestionList(aiParsedQuestions);
    }

    /**
     * 调用大模型API解析题目
     * @param fileContent 文件内容
     * @return AI解析的题目列表
     */
    private List<AiParsedQuestion> callAiModel(String fileContent) {
        try {
            // 构建请求给大模型的提示
            String prompt = buildPrompt(fileContent);
            
            // 调用实际的AI API
            String response = callActualAiApi(prompt);
            
            // 解析AI返回的结果
            return parseAiResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI解析题目失败: " + e.getMessage());
        }
    }

    /**
     * 构建发送给大模型的提示
     * @param fileContent 文件内容
     * @return 提示字符串
     */
    private String buildPrompt(String fileContent) {
        return String.format(
            "请仔细分析以下考试题库文件内容，识别其中的题目、选项、答案和解析，并以JSON格式返回。" +
            "返回格式应为一个数组，数组中每个元素代表一道题，包含以下字段：" +
            "{type: 题目类型(SINGLE_CHOICE单选题、MULTIPLE_CHOICE多选题、TRUE_FALSE判断题、FILL_BLANK填空题、ESSAY问答题)，" +
            "content: 题目内容，answer: 答案，analysis: 解析，difficulty: 难度(EASY简单、MEDIUM中等、HARD困难)}。" +
            "如果是选择题，还需要包含options字段，其中包含选项列表，每个选项包含{optionKey: 选项键(A、B、C、D)，content: 选项内容，isCorrect: 是否为正确答案}。" +
            "请确保返回的是有效的JSON格式数据，不要添加任何其他解释性文字。" +
            "\n\n文件内容：\n%s", fileContent);
    }

    /**
     * 解析AI返回的响应
     * @param response AI返回的原始响应
     * @return 解析后的题目列表
     */
    private List<AiParsedQuestion> parseAiResponse(String response) {
        try {
            // 解析AI返回的JSON响应
            JSONObject jsonResponse = JSON.parseObject(response);
            
            // 尝试从不同的可能结构中提取数据
            String parsedContent = "";
            if (jsonResponse.containsKey("choices")) {
                // OpenAI格式
                parsedContent = jsonResponse.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content");
            } else if (jsonResponse.containsKey("output")) {
                // 阿里云通义千问格式
                parsedContent = jsonResponse.getJSONObject("output").getString("text");
            } else {
                // 直接返回的内容
                parsedContent = response;
            }
            
            // 解析JSON数组
            List<AiParsedQuestion> result = new ArrayList<>();
            
            // 清理可能的非JSON内容
            String jsonStr = parsedContent.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7, jsonStr.lastIndexOf("``")).trim();
            } else if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3, jsonStr.lastIndexOf("``")).trim();
            }
            
            // 解析为数组
            result = JSON.parseArray(jsonStr, AiParsedQuestion.class);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 实际调用大模型API的方法
     * @param prompt 提示内容
     * @return AI返回的原始响应
     */
    public String callActualAiApi(String prompt) {
        // 这里应该实现实际的API调用逻辑
        // 示例：调用阿里云通义千问API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 注意：在实际部署时，需要配置真实的API密钥
        // 从配置或环境变量中获取API密钥
        String apiKey = System.getenv("DASHSCOPE_API_KEY"); // 从环境变量获取
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "YOUR_ACTUAL_API_KEY"; // 默认值，需要替换为实际密钥
        }
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-turbo"); // 或其他模型名称，兼容OpenAI格式
        
        // 构建消息数组 - 兼容OpenAI格式
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);
        
        // 参数配置
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", 0.1); // 较低的温度值以获得更一致的输出
        requestBody.put("parameters", parameters);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            // 替换为实际的API端点 - 使用兼容模式
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", 
                request, 
                String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果实际API调用失败，返回模拟响应
            return createMockResponse(prompt);
        }
    }
    
    /**
     * 创建模拟响应（当实际API不可用时使用）
     * @param prompt 提示内容
     * @return 模拟的响应
     */
    private String createMockResponse(String prompt) {
        // 在实际部署时，这应该被移除或替换为实际的错误处理
        System.out.println("警告：正在使用模拟AI响应。");
        
        // 返回一个空的JSON数组作为默认响应
        return "{\"choices\":[{\"message\":{\"content\":\"[]\"}}]}";
    }

    /**
     * 将AI解析结果转换为系统需要的Question对象列表
     * @param aiParsedQuestions AI解析的题目列表
     * @return Question对象列表
     */
    private List<Question> convertToQuestionList(List<AiParsedQuestion> aiParsedQuestions) {
        List<Question> questions = new ArrayList<>();
        
        for (AiParsedQuestion aiQuestion : aiParsedQuestions) {
            Question question = new Question();
            
            // 设置基本信息
            question.setType(aiQuestion.getType());
            question.setContent(aiQuestion.getContent());
            question.setAnswer(aiQuestion.getAnswer());
            question.setAnalysis(aiQuestion.getAnalysis());
            question.setDifficulty(aiQuestion.getDifficulty() != null ? aiQuestion.getDifficulty() : "MEDIUM");
            
            // 如果是选择题，设置选项
            if (aiQuestion.getOptions() != null && !aiQuestion.getOptions().isEmpty()) {
                List<QuestionOption> options = new ArrayList<>();
                for (AiParsedOption aiOption : aiQuestion.getOptions()) {
                    QuestionOption option = new QuestionOption();
                    option.setOptionKey(aiOption.getOptionKey());
                    option.setContent(aiOption.getContent());
                    option.setIsCorrect(aiOption.getIsCorrect() != null ? aiOption.getIsCorrect() : false);
                    options.add(option);
                }
                question.setOptions(options);
            }
            
            questions.add(question);
        }
        
        return questions;
    }
}