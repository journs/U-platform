package org.example.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class UserInfo {
    private String accessToken;
    private String refreshToken;
    private String tracer;
    private String name;           // 用于文件名（如 "古雅应"）
    private String mobile;         // 手机号（可选）
    private long loginTime;        // 登录时间（用于有效期判断）

    // Getter & Setter
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTracer() { return tracer; }
    public void setTracer(String tracer) { this.tracer = tracer; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
}