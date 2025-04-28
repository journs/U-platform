package org.example.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class LoginSubmit {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // 1. 创建 HTTP 服务实例
        HttpService httpService = new HttpService();
        LoginCheckup checkup = new LoginCheckup(httpService);

        // 2. 获取加密密钥
        String encryptionKey = getEncryptionKey(checkup);
        System.out.println("提取的 encryptionKey:");
        System.out.println(encryptionKey);

        // 3. 构造登录请求-------需要自己写账号密码
        LoginRequest loginRequest = buildLoginRequest(encryptionKey);
        String r = buildR(loginRequest);

        // 4. 加密生成 cryptogram
        String cryptogram = encryptData(r, encryptionKey);

        // 5. 构造 submit 请求体
        JsonObject submitRequestBody = buildSubmitRequestBody(encryptionKey, loginRequest.getMode(), cryptogram);

        // 6. 发送登录请求
        String response = sendLoginRequest(httpService, submitRequestBody);

        // 7. 输出结果
        System.out.println("Login Submit Response:");
        System.out.println(response);
    }

    private static String getEncryptionKey(LoginCheckup checkup) {
        String requestBody = "{\"mode\":\"Password\"}";
        return checkup.fetchEncryptionKey(requestBody);
    }

    private static LoginRequest buildLoginRequest(String encryptionKey) {
        LoginRequest e = new LoginRequest();
        e.setEncryptionKey(encryptionKey);
        // e.setIdentifier("账号xx");
        // e.setPassword("xxxx");
        e.setMode("Password");
        return e;
    }

    private static String buildR(LoginRequest e) {
        Map<String, Object> rData = new HashMap<>();
        rData.put("identifier", e.getIdentifier());
        rData.put("password", e.getPassword());
        if (e.hasOrganId()) {
            rData.put("organId", e.getOrganId());
        }
        return gson.toJson(rData);
    }

    private static String encryptData(String r, String encryptionKey) {
        RsaEncryptor encryptor = new RsaEncryptor();
        encryptor.setPublicKey(encryptionKey);
        return encryptor.encrypt(r);
    }

    private static JsonObject buildSubmitRequestBody(String encryptionKey, String mode, String cryptogram) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("encryptionKey", encryptionKey);
        requestBody.addProperty("mode", mode);
        requestBody.addProperty("cryptogram", cryptogram);
        return requestBody;
    }

    private static String sendLoginRequest(HttpService httpService, JsonObject requestBody) {
        return httpService.sendPost(
                "https://uc.eduplus.net/spi/login/submit",
                requestBody.toString()
        );
    }
}

// 辅助类：登录参数模型（模拟前端 e 对象）
class LoginRequest {
    private String encryptionKey;
    private String identifier;
    private String password;
    private String mode;
    private String organId; // 若无则为 null

    // Getter & Setter
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String key) {
        this.encryptionKey = key;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String id) {
        this.identifier = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getOrganId() {
        return organId;
    }

    public void setOrganId(String organId) {
        this.organId = organId;
    }

    public boolean hasOrganId() {
        return organId != null && !organId.isEmpty();
    }
}