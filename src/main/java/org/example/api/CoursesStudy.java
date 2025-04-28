package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.model.UserInfo;

import java.util.HashMap;
import java.util.Map;

public class CoursesStudy {
    private final HttpService httpService;
    private final LoginModule loginModule;

    public CoursesStudy(HttpService httpService, LoginModule loginModule) {
        this.httpService = httpService;
        this.loginModule = loginModule;
    }

    /**
     * 获取课程列表（理论课+实训课）
     */
    public String fetchCourses() {
        String url = "https://www.eduplus.net/api/course/athena/courses/study?types=Theory%2CTrain";
        Map<String, String> headers = buildRequestHeaders();
        return httpService.sendGetRequest(url, headers);
    }

    private Map<String, String> buildRequestHeaders() {
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
        headers.put("sec-ch-ua-platform", "Windows"); // 注意：去除原 fetch 中的引号（浏览器自动处理）
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");

        // 推荐添加的其他必要头（根据抓包补充）
        headers.put("Referer", "https://www.eduplus.net/workbench/index/school"); // fetch 中的 referrer
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");

        return headers;
    }

    public JsonArray parseCourseData(String jsonData) {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(jsonData, JsonObject.class);

        // 提取 data 数组
        JsonArray dataArray = root.getAsJsonArray("data");

        // 遍历每个课程对象
        for (int i = 0; i < dataArray.size(); i++) {
            JsonObject course = dataArray.get(i).getAsJsonObject();

            // 提取基础字段
            String courseId = course.get("id").getAsString();
            String courseName = course.get("name").getAsString();
            String type = course.get("type").getAsString();
            boolean videoDrag = course.get("videoDrag").getAsBoolean();

            // 处理嵌套的 teachClasses 数组
            JsonArray teachClasses = course.getAsJsonArray("teachClasses");
            if (!teachClasses.isJsonNull() && teachClasses.size() > 0) {
                JsonObject firstTeachClass = teachClasses.get(0).getAsJsonObject();
                String teachClassName = firstTeachClass.get("name").getAsString();
                String courseCode = firstTeachClass.get("code").getAsString();
                System.out.println("课程班级: " + teachClassName + ", 班级代码: " + courseCode);
            }

            // 输出课程信息
            System.out.println("课程 ID: " + courseId);
            System.out.println("课程名称: " + courseName);
            System.out.println("课程类型: " + type);
            System.out.println("是否允许拖拽视频: " + videoDrag);
            System.out.println("------------------------");
        }
        return dataArray;
    }

}