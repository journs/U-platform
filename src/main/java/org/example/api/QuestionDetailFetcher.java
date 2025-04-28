package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.model.UserInfo;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailFetcher {
    private final HttpService httpService;
    private final LoginModule loginModule;

    public QuestionDetailFetcher(HttpService httpService, LoginModule loginModule) {
        this.httpService = httpService;
        this.loginModule = loginModule;
    }

    /**
     * 获取题目详情
     * @param questionId 题目 ID
     * @param courseId   课程 ID（从课程信息中获取）
     * @param homeworkId 作业 ID（从作业信息中获取）
     * @return 题目详情响应字符串
     */
    public String fetchQuestionDetail(String questionId, String courseId, String homeworkId) {
        String url = "https://www.eduplus.net/api/course/homeworkQuestions/" + questionId + "/student/detail";
        Map<String, String> headers = buildRequestHeaders(courseId, homeworkId);
        return httpService.sendGetRequest(url, headers);
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
        headers.put("sec-ch-ua-platform", "Windows");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");

        // 动态生成 Referer（包含 courseId 和 homeworkId）
        headers.put("Referer", "https://www.eduplus.net/course/workAnswer/" + courseId + "/" + homeworkId + "/true?isPiYue=noPiYue");

        // User-Agent 头
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");

        return headers;
    }

    // 示例：解析题目详情（可根据实际响应结构扩展）
    public void parseQuestionDetail(String jsonData) {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(jsonData, JsonObject.class);

        // 这里可添加具体解析逻辑，例如：
        // String questionTitle = root.get("title").getAsString();
        // 请根据实际响应结构补充解析代码

        System.out.println("题目详情响应：");
        System.out.println(jsonData);
    }

    // 示例主方法
    public static void main(String[] args) {
        HttpService httpService = new HttpService();
        LoginModule loginModule = new LoginModule(httpService);
        QuestionDetailFetcher detailFetcher = new QuestionDetailFetcher(httpService, loginModule);

        // 假设已登录（实际需先调用 login()）
        loginModule.login("你的账号", "你的密码");

        // 替换为实际参数
        String questionId = "9202bfe3e1ba425f93c25142570035ef";
        String courseId = "ac583695451c43779c1bdb042ebc465b";
        String homeworkId = "cc88c545dd584305a6521e72568ebbd0";

        String response = detailFetcher.fetchQuestionDetail(questionId, courseId, homeworkId);
        detailFetcher.parseQuestionDetail(response);
    }
}