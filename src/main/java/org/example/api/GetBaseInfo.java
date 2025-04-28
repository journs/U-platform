package org.example.api;

import org.example.model.UserInfo;

import java.util.HashMap;
import java.util.Map;

public class GetBaseInfo {
    private final HttpService httpService;
    private final LoginModule loginModule;

    public GetBaseInfo(HttpService httpService, LoginModule loginModule) {
        this.httpService = httpService;
        this.loginModule = loginModule;
    }

    /**
     * 获取用户基本信息（Map 方式构建请求头，兼容性更强）
     */
    public String fetchBasicInfo() {
        String url = "https://www.eduplus.net/api/portal/users/basic_info";
        Map<String, String> headers = buildAuthHeaders(); // 使用 Map 替代 HttpHeaders
        return httpService.sendGetRequest(url, headers);
    }

    private Map<String, String> buildAuthHeaders() {
        System.out.println(loginModule);
        // 增强版登录状态校验（避免空指针）
        UserInfo userInfo = loginModule.getUserInfo();
        if (userInfo == null || userInfo.getAccessToken() == null) {
            throw new IllegalStateException("未登录或 Token 无效，请先调用 login() 方法");
        }

        Map<String, String> headers = new HashMap<>();

        // 认证头（修正：Cookie 应为登录时获取的 Session Cookie，非 Access Token）
        headers.put("X-Access-Token", userInfo.getAccessToken());
        headers.put("Cookie", userInfo.getAccessToken()); // 关键：使用 Session Cookie（需 UserInfo 包含该字段）

        // 浏览器指纹头（与抓包完全一致）
        headers.put("Referer", "https://www.eduplus.net/home/index");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "Windows");

        return headers;
    }

    // 移除原 validateLoginStatus 方法，直接在 buildAuthHeaders 中校验（更简洁）
}