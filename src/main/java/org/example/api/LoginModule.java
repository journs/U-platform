package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.example.model.UserInfo;
import org.example.util.LocalStorage;
import org.example.vo.LoginResponse;

import java.util.HashMap;
import java.util.Map;

public class LoginModule {
    private final HttpService httpService;
    private final LoginCheckup loginCheckup;
    private final Gson gson = new Gson();
    private UserInfo userInfo;

    public LoginModule(HttpService httpService) {
        this.httpService = httpService;
        this.loginCheckup = new LoginCheckup(httpService);
    }

    /**
     * 执行登录并保存用户信息
     */
    public UserInfo login(String identifier, String password) {
        // 1. 获取加密密钥
        String encryptionKey = fetchEncryptionKey();

        // 2. 构造并加密登录数据
        LoginRequest loginRequest = buildLoginRequest(encryptionKey, identifier, password);
        String r = buildR(loginRequest);
        String cryptogram = encryptData(r, encryptionKey);

        // 3. 发送登录请求并解析响应
        String loginResponseJson = sendLoginRequest(cryptogram, encryptionKey);
        LoginResponse loginResponse = parseLoginResponse(loginResponseJson);
        UserInfo userInfo = new UserInfo();
        userInfo.setAccessToken(loginResponse.getAccessToken());   // 保存 AccessToken
        userInfo.setRefreshToken(loginResponse.getRefreshToken()); // 保存 RefreshToken
        userInfo.setTracer(loginResponse.getTracer());             // 保存 Tracer
        this.userInfo = userInfo; // 正确赋值

        // 4. 获取用户基本信息（包含 name 和 mobile）
        UserBasicInfo basicInfo = fetchUserBasicInfo(loginResponse.getAccessToken());

        // 5. 组装用户信息（关键：将 Token 保存到 UserInfo）
        userInfo.setName(basicInfo.getName());                     // 保存用户名
        userInfo.setMobile(identifier);                    // 保存手机号
        userInfo.setLoginTime(System.currentTimeMillis());        // 记录登录时间

        // 6. 保存到本地存储
        LocalStorage.saveUserInfo(userInfo);
        this.userInfo = userInfo; // 保存到内存

        return userInfo;
    }

    /**
     * 根据手机号码获取本地有效用户信息（优先内存，其次本地文件）
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }
    public UserInfo getUserInfo(String mobile) {
        // 优先使用内存中的用户信息
        if (userInfo != null && userInfo.getMobile() != null && userInfo.getMobile().equals(mobile) && LocalStorage.isTokenValid(userInfo)) {
            return userInfo;
        }
        // 从本地文件加载（使用手机号码作为文件名）
        UserInfo localUser = LocalStorage.loadUserInfo(mobile);
        if (localUser != null && LocalStorage.isTokenValid(localUser)) {
            this.userInfo = localUser; // 更新内存中的用户信息
            return localUser;
        }
        return null;
    }

    // ===================== 内部方法 =====================

    private String fetchEncryptionKey() {
        String requestBody = "{\"mode\":\"Password\"}";
        return loginCheckup.fetchEncryptionKey(requestBody);
    }

    private LoginRequest buildLoginRequest(String encryptionKey, String identifier, String password) {
        LoginRequest e = new LoginRequest();
        e.setEncryptionKey(encryptionKey);
        e.setIdentifier(identifier);
        e.setPassword(password);
        e.setMode("Password");
        return e;
    }

    private String buildR(LoginRequest e) {
        Map<String, Object> rData = new HashMap<>();
        rData.put("identifier", e.getIdentifier());
        rData.put("password", e.getPassword());
        return gson.toJson(rData);
    }

    private String encryptData(String r, String encryptionKey) {
        RsaEncryptor encryptor = new RsaEncryptor();
        encryptor.setPublicKey(encryptionKey);
        return encryptor.encrypt(r);
    }

    private String sendLoginRequest(String cryptogram, String encryptionKey) {
        JsonObject requestBody = buildSubmitRequestBody(encryptionKey, cryptogram);
        return httpService.sendPost(
                "https://uc.eduplus.net/spi/login/submit",
                requestBody.toString()
        );
    }

    private JsonObject buildSubmitRequestBody(String encryptionKey, String cryptogram) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("encryptionKey", encryptionKey);
        requestBody.addProperty("mode", "Password");
        requestBody.addProperty("cryptogram", cryptogram);
        return requestBody;
    }

    private LoginResponse parseLoginResponse(String responseBody) {
        try {
            return gson.fromJson(responseBody, LoginResponse.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("登录响应解析失败: " + e.getMessage(), e);
        }
    }

    private UserBasicInfo fetchUserBasicInfo(String accessToken) {
        GetBaseInfo getBaseInfo = new GetBaseInfo(httpService, this);
        String response = getBaseInfo.fetchBasicInfo(); // 假设该方法已包含 Token 校验
        return gson.fromJson(response, UserBasicInfo.class);
    }




    /**
     * 用户基本信息模型（根据实际响应结构修正）
     */
    private static class UserBasicInfo {
        private Data data;

        public String getName() {
            return data != null ? data.name : "";
        }

        public String getMobile() {
            return data != null ? data.mobile : "";
        }

        private static class Data {
            private String name;
            private String mobile;
            // 按实际响应添加其他字段（如 userid, organName 等）
        }
    }
}