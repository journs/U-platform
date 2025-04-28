package org.example.vo;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private int code;
    private Data data;
    private boolean success;
    private String tracer;
    private String message;
    private int status;

    // Getter 方法
    public String getAccessToken() {
        return data != null ? data.getAccessToken() : null;
    }

    public String getRefreshToken() {
        return data != null ? data.getRefreshToken() : null;
    }
    public String getTracer() {
        return data != null ? data.getTracer() : null;
    }

    // 内部 Data 类
    private static class Data {
        @SerializedName("accessToken")
        private String accessToken;
        @SerializedName("refreshToken")
        private String refreshToken;
        @SerializedName("tracer")
        private String tracer;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
        public String getTracer() {
            return tracer;
        }
    }
}