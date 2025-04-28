package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.vo.LoginCheckupResponse;

public class LoginCheckup {
    private final HttpService httpService;
    private final Gson gson = new Gson();

    public LoginCheckup(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * 发送 checkup 请求并解析获取 encryptionKey
     */
    public String fetchEncryptionKey(String requestBody) {
        String responseBody = sendCheckupRequest(requestBody);
        return parseEncryptionKey(responseBody);
    }

    private String sendCheckupRequest(String requestBody) {
        String url = "https://uc.eduplus.net/spi/login/checkup";
        return httpService.sendPostRequest(url, requestBody);
    }

    private String parseEncryptionKey(String responseBody) {
        try {
            LoginCheckupResponse response = gson.fromJson(responseBody, LoginCheckupResponse.class);
            if (response == null || response.getEncryptionKey() == null) {
                throw new IllegalStateException("响应中未找到 encryptionKey");
            }
            return response.getEncryptionKey();
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("JSON 解析失败", e);
        }
    }
}