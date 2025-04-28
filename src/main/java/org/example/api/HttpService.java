package org.example.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class HttpService {
    private final HttpClient client;

    public HttpService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * 发送 POST 请求并返回处理后的响应数据（自动处理压缩和编码）
     */
    public String sendPostRequest(String url, String requestBody) {
        HttpRequest request = buildHttpRequest(url, requestBody);
        HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("请求发送失败", e);
        }

        byte[] responseBytes = handleCompression(response);
        return decodeResponse(responseBytes, response.headers());
    }

    /**
     * 发送 POST 请求（兼容登录提交，使用统一的压缩/编码处理）
     */
    public String sendPost(String url, String requestBody) {
        return sendPostRequest(url, requestBody); // 复用通用处理逻辑
    }

    private HttpRequest buildHttpRequest(String url, String requestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("sec-ch-ua-platform", "Windows")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("sec-ch-ua", "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"")
                .header("Content-Type", "application/json")
                .header("sec-ch-ua-mobile", "?0")
                .header("Origin", "https://uc.eduplus.net")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Dest", "empty")
                .header("Referer", "https://uc.eduplus.net/login?theme=athena&layout=e30&redirect_uri=https%3A%2F%2Fwww.eduplus.net%2Fauthorize%3Fredirect%3Dhttps%253A%252F%252Fwww.eduplus.net%252Fhome%252Findex")
                .header("Accept-Encoding", "gzip, deflate, br, zstd")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
    }

    private byte[] handleCompression(HttpResponse<byte[]> response) {
        HttpHeaders headers = response.headers();
        String contentEncoding = headers.firstValue("Content-Encoding").orElse("").toLowerCase();
        byte[] bytes = response.body();

        try {
            switch (contentEncoding) {
                case "gzip":
                    try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
                        return is.readAllBytes();
                    }
                case "deflate":
                    try (InputStream is = new InflaterInputStream(new ByteArrayInputStream(bytes))) {
                        return is.readAllBytes();
                    }
                default:
                    return bytes;
            }
        } catch (IOException e) {
            throw new RuntimeException("压缩解压缩失败", e);
        }
    }

    private String decodeResponse(byte[] bytes, HttpHeaders headers) {
        String contentType = headers.firstValue("Content-Type").orElse("");
        Charset charset = detectCharsetFromContentType(contentType);

        if (charset == null) {
            charset = tryDetectCharset(bytes);
        }

        return new String(bytes, charset);
    }

    private Charset detectCharsetFromContentType(String contentType) {
        if (contentType.contains("charset=")) {
            String encoding = contentType.split("charset=")[1].split(";")[0].trim();
            try {
                return Charset.forName(encoding);
            } catch (Exception e) {
                System.out.println("不支持的编码: " + encoding);
            }
        }
        return null;
    }

    private Charset tryDetectCharset(byte[] bytes) {
        String[] encodings = {"UTF-8", "GBK", "ISO-8859-1", "UTF-16"};
        for (String encoding : encodings) {
            try {
                String temp = new String(bytes, encoding);
                if (temp.contains("{") || temp.contains("\"")) { // 简单判断是否为 JSON
                    return Charset.forName(encoding);
                }
            } catch (Exception e) {
                System.out.println("解码 " + encoding + " 失败: " + e.getMessage());
            }
        }
        return StandardCharsets.UTF_8; // 默认返回 UTF-8
    }

    /**
     * 发送 GET 请求（自动处理压缩和编码）
     */
    public String sendGetRequest(String url, Map<String, String> headers) {
        HttpRequest request = buildHttpRequest(url, headers, "GET", null);
        HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("GET 请求失败", e);
        }

        byte[] responseBytes = handleCompression(response);
        return decodeResponse(responseBytes, response.headers());
    }

    private HttpRequest buildHttpRequest(String url, Map<String, String> headers, String method, String requestBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        // 添加固定指纹头（与浏览器一致）
        builder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
                .header("sec-ch-ua", "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "Windows");

        // 添加自定义头（从 Map 转换）
        if (headers != null) {
            headers.forEach(builder::header); // 直接添加头字段（单值）
        }

        // 设置请求方法
        if ("GET".equals(method)) {
            builder.GET();
        } else if ("POST".equals(method)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
        } else if ("PUT".equals(method)) {
            builder.PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
        }

        return builder.build();
    }

    /**
     * 发送 PUT 请求（自动处理压缩和编码）
     */
    public String sendPutRequest(String url, Map<String, String> headers, String requestBody) {
        HttpRequest request = buildHttpRequest(url, headers, "PUT", requestBody);
        HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("PUT 请求失败", e);
        }

        byte[] responseBytes = handleCompression(response);
        return decodeResponse(responseBytes, response.headers());
    }

    /**
     * 发送 POST 请求，携带自定义请求头和请求体
     */
    public String sendPostRequest(String url, Map<String, String> headers, String requestBody) {
        HttpRequest request = buildHttpRequest(url, headers, "POST", requestBody);
        HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("POST 请求失败", e);
        }

        byte[] responseBytes = handleCompression(response);
        return decodeResponse(responseBytes, response.headers());
    }
}