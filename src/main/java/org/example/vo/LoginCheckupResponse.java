package org.example.vo;

import com.google.gson.annotations.SerializedName;

public class LoginCheckupResponse {
    private int code;
    private Data data;
    private boolean success;
    private String tracer;
    private String message;
    private int status;

    // Getter 方法（仅提取需要的 encryptionKey，其他字段按需添加）
    public String getEncryptionKey() {
        return data != null ? data.getEncryptionKey() : null;
    }

    // 内部 Data 类
    private static class Data {
        @SerializedName("encryptionKey")
        private String encryptionKey;
        private Object authenticationModeList; // 若无需求，可设为 Object 或忽略

        public String getEncryptionKey() {
            return encryptionKey;
        }
    }
}