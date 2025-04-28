package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.model.UserInfo;

import java.util.HashMap;
import java.util.Map;

public class AnswerSubmitter {
    private final HttpService httpService;
    private final LoginModule loginModule;

    public AnswerSubmitter(HttpService httpService, LoginModule loginModule) {
        this.httpService = httpService;
        this.loginModule = loginModule;
    }

    /**
     * 提交题目答案
     * @param homeworkQuestionId 题目 ID
     * @param userAnswer         用户答案（如 "A"）
     * @param courseId           课程 ID
     * @param homeworkId         作业 ID
     * @return 服务器响应字符串
     */
    public String submitAnswer(
            String homeworkQuestionId,
            String userAnswer,
            String courseId,
            String homeworkId
    ) {
        String url = "https://www.eduplus.net/api/course/hwAnswers/answer";
        Map<String, String> headers = buildRequestHeaders(courseId, homeworkId);
        String requestBody = buildRequestBody(homeworkQuestionId, userAnswer);
        return httpService.sendPutRequest(url, headers, requestBody);
    }

    private Map<String, String> buildRequestHeaders(String courseId, String homeworkId) {
        UserInfo userInfo = loginModule.getUserInfo();
        if (userInfo == null || userInfo.getAccessToken() == null) {
            throw new IllegalStateException("未登录或 Token 无效，请先调用 login()");
        }

        Map<String, String> headers = new HashMap<>();

        // 认证头
        headers.put("X-Access-Token", userInfo.getAccessToken());

        // 浏览器指纹头（与 fetch 请求完全一致）
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "Windows"); // 去除引号，与浏览器行为一致
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");

        // 构造 Referer（包含 courseId 和 homeworkId）
        headers.put("Referer",
                "https://www.eduplus.net/course/workAnswer/" + courseId + "/" + homeworkId + "/true?isPiYue=noPiYue"
        );

        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
        headers.put("Content-Type", "application/json"); // 添加请求体类型

        return headers;
    }

    private String buildRequestBody(String homeworkQuestionId, String userAnswer) {
        JsonObject body = new JsonObject();
        body.addProperty("homeworkQuestionId", homeworkQuestionId);
        body.addProperty("userAnswer", userAnswer);
        return new Gson().toJson(body);
    }

    // 示例：主方法调用
    public static void main(String[] args) {
        HttpService httpService = new HttpService();
        LoginModule loginModule = new LoginModule(httpService);

        // 假设已登录
        loginModule.login("你的账号", "你的密码");

        AnswerSubmitter submitter = new AnswerSubmitter(httpService, loginModule);
        String homeworkQuestionId = "9202bfe3e1ba425f93c25142570035ef";
        String userAnswer = "B"; // 假设正确答案为 B
        String courseId = "ac583695451c43779c1bdb042ebc465b";
        String homeworkId = "cc88c545dd584305a6521e72568ebbd0";

        String response = submitter.submitAnswer(
                homeworkQuestionId,
                userAnswer,
                courseId,
                homeworkId
        );
        System.out.println("答案提交响应：" + response);
    }
}