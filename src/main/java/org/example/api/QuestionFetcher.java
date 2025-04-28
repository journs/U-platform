package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.model.UserInfo;

import java.util.HashMap;
import java.util.Map;

public class QuestionFetcher {
    private final HttpService httpService;
    private final LoginModule loginModule;

    public QuestionFetcher(HttpService httpService, LoginModule loginModule) {
        this.httpService = httpService;
        this.loginModule = loginModule;
    }


    /**
     * 解析并打印题目详细信息（新增方法，替代原 main 中的模拟逻辑）
     *
     * @return
     */
    public JsonArray printQuestionDetails(String jsonData) {
        Gson gson = new Gson();
        try {
            JsonObject root = gson.fromJson(jsonData, JsonObject.class);
            JsonArray dataArray = root.getAsJsonArray("data");

            System.out.println("------------------------ 题目信息 ------------------------");
            System.out.printf("%-15s %-10s %s%n", "题目 ID", "题号", "题目类型（qsnType）");
            System.out.println("---------------------------------------------------------");

            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject question = dataArray.get(i).getAsJsonObject();
                String id = getSafeString(question, "id");
                int orderNumber = getSafeInt(question, "orderNumber");
                int qsnType = getSafeInt(question, "qsnType");

                System.out.printf("%-15s %-10d %d%n", id, orderNumber, qsnType);
            }

            System.out.println("---------------------------------------------------------");
            return dataArray;
        } catch (Exception e) {
            throw new RuntimeException("题目详情解析失败: " + e.getMessage(), e);
        }
    }

    private String getSafeString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private int getSafeInt(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : -1;
    }

    private Map<String, String> buildRequestHeaders(String homeworkId,String courseId) {
        // 保持原有逻辑不变
        UserInfo userInfo = loginModule.getUserInfo();
        if (userInfo == null || userInfo.getAccessToken() == null) {
            throw new IllegalStateException("未登录或 Token 无效，请先调用 login()");
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Access-Token", userInfo.getAccessToken());
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "Windows");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("Referer", "https://www.eduplus.net/course/workAnswer/" + courseId + "/" + homeworkId + "/true?isPiYue=noPiYue");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
        return headers;
    }

    // main 方法简化为调用示例
    public static void main(String[] args) {
        HttpService httpService = new HttpService();
        LoginModule loginModule = new LoginModule(httpService);

        String homeworkId = "51726f4303779c3438fb39561410";
    }

    // 新增：直接获取响应字符串（如需复用）
    public String fetchHomeworkQuestionsResponse(JsonObject homework, String courseId) {
        String url = "https://www.eduplus.net/api/course/homeworkQuestions/student?homeworkId=" + homework.get("homeworkId").getAsString();

        String homeworkId = homework.get("id").getAsString();
        Map<String, String> headers = buildRequestHeaders(homeworkId,courseId);
        return httpService.sendGetRequest(url, headers);
    }
}