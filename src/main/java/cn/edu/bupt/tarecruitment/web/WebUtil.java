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

    private WebUtil() {
    }

    public static Map<String, String> parseQuery(String rawQuery) {
        return parseKeyValuePairs(rawQuery);
    }

    public static Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseKeyValuePairs(requestBody);
    }

    public static MultipartFormData parseMultipart(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        byte[] body = exchange.getRequestBody().readAllBytes();
        return MultipartFormData.parse(contentType, body);
    }

    public static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] responseBody = html.getBytes(StandardCharsets.UTF_8);
        
        
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        
       
        
        exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate");
        
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

    
    String finalContentType = (contentType != null) ? contentType : "application/octet-stream";
    String finalDownloadName = (downloadName != null) ? downloadName : file.getFileName().toString();

   
    var headers = exchange.getResponseHeaders();
    headers.set("Content-Type", finalContentType);
    headers.set("Content-Disposition", buildContentDisposition(finalDownloadName));

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
        String safeName =
                downloadName.replace("\r", "").replace("\n", "").replace("\"", "'");
        return "attachment; filename=\""
                + safeName
                + "\"; filename*=UTF-8''"
                + URLEncoder.encode(safeName, StandardCharsets.UTF_8);
    }
}
