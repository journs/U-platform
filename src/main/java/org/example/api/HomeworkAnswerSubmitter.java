package org.example.api;

import org.example.model.UserInfo;

import java.util.HashMap;
import java.util.Map;

public class HomeworkAnswerSubmitter {
    private final HttpService httpService;
    private final LoginModule loginModule;

    public HomeworkAnswerSubmitter(HttpService httpService, LoginModule loginModule) {
        this.httpService = httpService;
        this.loginModule = loginModule;
    }

    /**
     * 提交作业答案（URL 参数传递，修复受限头错误）
     */
    public String submitAnswer(String homeworkPublishId) {
        String url = "https://www.eduplus.net/api/course/hwAnswerSheets?homeworkPublishId=" + homeworkPublishId;
        Map<String, String> headers = buildRequestHeaders();
        String requestBody = ""; // 空请求体（根据抓包 Content-Length: 0）

        return httpService.sendPostRequest(url, headers, requestBody);
    }

    private Map<String, String> buildRequestHeaders() {
        UserInfo userInfo = loginModule.getUserInfo();
        if (userInfo == null || userInfo.getAccessToken() == null) {
            throw new IllegalStateException("未登录或 Token 无效，请先调用 login()");
        }

        Map<String, String> headers = new HashMap<>();

        // 1. 认证头（合法头，非受限）
        headers.put("X-Access-Token", userInfo.getAccessToken());

        // 2. 浏览器指纹头（合法头，非受限）
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "Windows");

        // 3. 来源与安全头（合法头，非受限）
        headers.put("Origin", "https://www.eduplus.net");
        headers.put("Referer", "https://www.eduplus.net/course/workAnswer/ac583695451c43779c1bdb042ebc465b/cc88c545dd584305a6521e72568ebbd0/true?isPiYue=noPiYue");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");

        // 4. 内容类型头（如果有请求体，此处为空可不加，但保留示例）
        headers.put("Content-Type", "application/json"); // 若请求体为 JSON 需添加，此处空体可移除

        return headers;
    }

    // 测试
    public static void main(String[] args) {
        HttpService httpService = new HttpService();
        LoginModule loginModule = new LoginModule(httpService);

        // 模拟登录（假设登录后获取 UserInfo，包含 accessToken）
        UserInfo userInfo = loginModule.login("账号", "密码");

        HomeworkAnswerSubmitter submitter = new HomeworkAnswerSubmitter(httpService, loginModule);
        String homeworkPublishId = "5a56d24c86fe45e4bac5f6369d7cf7d4";
        String response = submitter.submitAnswer(homeworkPublishId);

        System.out.println("提交响应：" + response);
    }

}