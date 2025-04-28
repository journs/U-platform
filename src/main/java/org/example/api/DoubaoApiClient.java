package org.example.api;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class DoubaoApiClient {
    private static final String DOUBAO_API_URL = "https://api.doubao.com/v1/chat"; // 假设豆包 API 地址
    private final HttpService httpService;

    public DoubaoApiClient(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * 发送题目详情到豆包 AI 并获取答案
     */
    public String getDoubaoAnswer(String questionText, String options) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "doubao-advanced");
        requestBody.put("messages", new Object[]{
                new HashMap<String, Object>() {{
                    put("role", "user");
                    put("content", buildQuestion(questionText, options));
                }}
        });

        // 发送 POST 请求（假设 HttpService 支持 POST）
        return httpService.sendPostRequest(DOUBAO_API_URL, new Gson().toJson(requestBody));
    }

    /**
     * 格式化题目为豆包 AI 可识别的提问格式
     */
    private String buildQuestion(String questionText, String options) {
        return "请回答以下问题：\n" +
                "题目：" + stripHtml(questionText) + "\n" +
                "选项：" + stripHtml(options) + "\n" +
                "请直接返回正确选项字母（如：A）";
    }

    /**
     * 去除 HTML 标签（如 <p> </p>）
     */
    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", "");
    }
}