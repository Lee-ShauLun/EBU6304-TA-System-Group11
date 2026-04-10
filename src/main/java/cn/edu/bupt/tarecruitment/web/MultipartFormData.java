package cn.edu.bupt.tarecruitment.web;

import cn.edu.bupt.tarecruitment.service.ValidationException;
import cn.edu.bupt.tarecruitment.util.HtmlUtil;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MultipartFormData {

    private final Map<String, String> fields;
    private final Map<String, UploadedFile> files;

    public MultipartFormData(Map<String, String> fields, Map<String, UploadedFile> files) {
        this.fields = fields;
        this.files = files;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public Map<String, UploadedFile> getFiles() {
        return files;
    }

    public static MultipartFormData parse(String contentType, byte[] body) {
        String boundary = extractBoundary(contentType);
        String delimiter = "--" + boundary;
        String raw = new String(body, StandardCharsets.ISO_8859_1);
        String[] chunks = raw.split(Pattern.quote(delimiter));

        Map<String, String> fields = new LinkedHashMap<>();
        Map<String, UploadedFile> files = new LinkedHashMap<>();

        for (String chunk : chunks) {
            String part = stripBoundaryPadding(chunk);
            if (part.isEmpty() || "--".equals(part)) {
                continue;
            }

            int separatorIndex = part.indexOf("\r\n\r\n");
            if (separatorIndex < 0) {
                continue;
            }

            String headersBlock = part.substring(0, separatorIndex);
            String contentBlock = part.substring(separatorIndex + 4);
            if (contentBlock.endsWith("\r\n")) {
                contentBlock = contentBlock.substring(0, contentBlock.length() - 2);
            }

            Map<String, String> headers = parseHeaders(headersBlock);
            String disposition = headers.getOrDefault("content-disposition", "");
            String fieldName = extractDispositionValue(disposition, "name");
            String fileName = extractDispositionValue(disposition, "filename");
            if (HtmlUtil.isBlank(fieldName)) {
                continue;
            }

            if (HtmlUtil.isBlank(fileName)) {
                fields.put(fieldName, contentBlock);
            } else {
                UploadedFile file =
                        new UploadedFile(
                                fieldName,
                                fileName,
                                headers.getOrDefault("content-type", "application/octet-stream"),
                                contentBlock.getBytes(StandardCharsets.ISO_8859_1));
                files.put(fieldName, file);
            }
        }

        return new MultipartFormData(fields, files);
    }

    private static String extractBoundary(String contentType) {
        if (HtmlUtil.isBlank(contentType) || !contentType.contains("boundary=")) {
            throw new ValidationException("The upload request is missing a multipart boundary.");
        }
        String[] parts = contentType.split("boundary=", 2);
        String boundary = parts[1].trim();
        if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() >= 2) {
            boundary = boundary.substring(1, boundary.length() - 1);
        }
        return boundary;
    }

    private static String stripBoundaryPadding(String chunk) {
        String part = chunk;
        while (part.startsWith("\r\n")) {
            part = part.substring(2);
        }
        while (part.endsWith("\r\n")) {
            part = part.substring(0, part.length() - 2);
        }
        if (part.endsWith("--")) {
            part = part.substring(0, part.length() - 2);
        }
        return part;
    }

    private static Map<String, String> parseHeaders(String headersBlock) {
        Map<String, String> headers = new LinkedHashMap<>();
        String[] lines = headersBlock.split("\r\n");
        for (String line : lines) {
            int separatorIndex = line.indexOf(':');
            if (separatorIndex <= 0) {
                continue;
            }
            String name = line.substring(0, separatorIndex).trim().toLowerCase();
            String value = line.substring(separatorIndex + 1).trim();
            headers.put(name, value);
        }
        return headers;
    }

    private static String extractDispositionValue(String disposition, String key) {
        String[] tokens = disposition.split(";");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.startsWith(key + "=")) {
                String value = trimmed.substring((key + "=").length()).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    return value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return "";
    }
}
