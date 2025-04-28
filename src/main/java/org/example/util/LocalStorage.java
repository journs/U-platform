package org.example.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.UserInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LocalStorage {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 保存用户信息到本地 JSON 文件（以手机号码命名）
     */
    public static void saveUserInfo(UserInfo userInfo) {
        String filename = userInfo.getMobile() + ".json";
        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            gson.toJson(userInfo, writer);
            System.out.println("用户信息已保存到 " + filename);
        } catch (IOException e) {
            throw new RuntimeException("保存用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 从本地加载用户信息（根据手机号码查找）
     */
    public static UserInfo loadUserInfo(String mobile) {
        String filename = mobile + ".json";
        File file = new File(filename);
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(filename, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, UserInfo.class);
        } catch (IOException e) {
            throw new RuntimeException("加载用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查 Token 是否有效（简化逻辑：存在且未过期，实际需校验有效期）
     */
    public static boolean isTokenValid(UserInfo userInfo) {
        if (userInfo == null || userInfo.getAccessToken() == null) return false;
        // 此处可添加有效期检查（如：当前时间 - loginTime < 有效期）
        return true;
    }
}