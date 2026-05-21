package cn.edu.bupt.tarecruitment.web;

import cn.edu.bupt.tarecruitment.service.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WebUtil {

    // 常量抽取，避免硬编码
    private static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final String DEFAULT_FILE_CONTENT_TYPE = "application/octet-stream";

    /**
     * 工具类私有构造器，禁止实例化
     */
    private WebUtil() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    public static Map<String, String> parseQuery(String rawQuery) {
        return parseKeyValuePairs(rawQuery);
    }

    public static Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        // 使用try-with-resources自动关闭流
        try (var inputStream = exchange.getRequestBody()) {
            String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parseKeyValuePairs(requestBody);
        }
    }

    public static MultipartFormData parseMultipart(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        byte[] body = exchange.getRequestBody().readAllBytes();
        return MultipartFormData.parse(contentType, body);
    }

    public static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] responseBody = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", HTML_CONTENT_TYPE);
        exchange.sendResponseHeaders(statusCode, responseBody.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBody);
        }
    }

    public static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    public static void sendFile(
            HttpExchange exchange, Path file, String contentType, String downloadName) throws IOException {
        if (!Files.exists(file)) {
            throw new ValidationException("The requested file does not exist.");
        }
        // 增加非普通文件校验
        if (!Files.isRegularFile(file)) {
            throw new ValidationException("The requested path is not a valid file.");
        }

        exchange.getResponseHeaders().set(
                "Content-Type", contentType == null ? DEFAULT_FILE_CONTENT_TYPE : contentType);
        exchange.getResponseHeaders().set(
                "Content-Disposition",
                buildContentDisposition(downloadName == null ? file.getFileName().toString() : downloadName));
        exchange.sendResponseHeaders(200, Files.size(file));
        try (OutputStream outputStream = exchange.getResponseBody()) {
            Files.copy(file, outputStream);
        }
    }

    private static Map<String, String> parseKeyValuePairs(String content) {
        Map<String, String> values = new LinkedHashMap<>();
        if (content == null || content.isEmpty()) {
            return values;
        }

        String[] pairs = content.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }

            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            values.put(key, value);
        }
        return values;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String buildContentDisposition(String downloadName) {
        String safeName = downloadName.replace("\r", "").replace("\n", "").replace("\"", "'");
        // 使用String.format优化字符串拼接
        return String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                safeName, URLEncoder.encode(safeName, StandardCharsets.UTF_8));
    }
}
