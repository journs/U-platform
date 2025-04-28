package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.model.UserInfo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

// 该类用于获取指定课程已发布的学生作业信息
public class HomeworkFetcher {
    private final HttpService httpService;
    private final LoginModule loginModule;

    // 构造函数，初始化 HttpService 和 LoginModule
    public HomeworkFetcher(HttpService httpService, LoginModule loginModule) {
        this.httpService = httpService;
        this.loginModule = loginModule;
    }

    // 获取指定课程已发布的学生作业信息
    public String fetchPublishedHomeworks(String courseId) {
        String url = "https://www.eduplus.net/api/course/homeworks/published/student?courseId=" + courseId;
        Map<String, String> headers = buildRequestHeaders(courseId);
        return httpService.sendGetRequest(url, headers);
    }

    // 构建请求头
    private Map<String, String> buildRequestHeaders(String courseId) {
        UserInfo userInfo = loginModule.getUserInfo();
        if (userInfo == null || userInfo.getAccessToken() == null) {
            throw new IllegalStateException("未登录或 Token 无效，请先调用 login()");
        }

        Map<String, String> headers = new HashMap<>();

        // 认证头（X-Access-Token 来自登录 Token）
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

        // 推荐添加的其他必要头（根据抓包补充）
        headers.put("Referer", "https://www.eduplus.net/course/course/" + courseId + "/HomeworkPage?userRole=");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");

        return headers;
    }

    // 解析获取到的作业信息并筛选未交作业
    public JsonArray parseHomeworkData(String jsonData) {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(jsonData, JsonObject.class);
        JsonArray dataArray = root.getAsJsonArray("data");

        System.out.println("------------------------ 作业提交状态 ------------------------");
        System.out.printf("%-15s %-20s %s%n", "作业名称", "截止时间", "提交状态（0=未交，1=已交）");
        System.out.println("-------------------------------------------------------------");

        for (int i = 0; i < dataArray.size(); i++) {
            JsonObject homework = dataArray.get(i).getAsJsonObject();
            JsonObject homeworkDTO = homework.getAsJsonObject("homeworkDTO"); // 获取嵌套的 homeworkDTO 对象

            String homeworkName = homeworkDTO.get("name").getAsString();
            long endTime = homework.get("endTime").getAsLong();
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());
            int subStatus = homeworkDTO.get("subStatus").getAsInt(); // 获取提交状态

            // 格式化时间戳为可读格式（示例：转换为毫秒级时间戳，实际可进一步转换为日期）
            String status = subStatus == 0 ? "未交" : "已交";

            // 输出作业信息
            System.out.printf("%-15s %-20s %s%n",
                    homeworkName,
                    dateTime,
                    status
            );
        }

        System.out.println("-------------------------------------------------------------");
        return dataArray;
    }
}