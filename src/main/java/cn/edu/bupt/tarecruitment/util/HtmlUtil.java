package cn.edu.bupt.tarecruitment.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class HtmlUtil {

    private HtmlUtil() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String nl2br(String value) {
        return escape(value).replace("\r\n", "<br>").replace("\n", "<br>");
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String nonNull(String value) {
        return value == null ? "" : value;
    }
}
