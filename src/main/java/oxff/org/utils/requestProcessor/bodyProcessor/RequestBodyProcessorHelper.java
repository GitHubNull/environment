package oxff.org.utils.requestProcessor.bodyProcessor;

import burp.api.montoya.http.message.HttpHeader;

import java.util.List;

public class RequestBodyProcessorHelper {
    /**
     * 从 Burp Suite Montoya API 的 headers 中提取 Content-Type 值。
     */
    public static String getContentType(List<HttpHeader> headers) {
        for (HttpHeader header : headers) {
            if (header.name().equalsIgnoreCase("Content-Type")) {
                return header.value();
            }
        }
        return null; // 如果没有找到 Content-Type，返回 null
    }



    /**
     * 从 Content-Type 中提取 boundary。
     */
    public static String getBoundary(String contentType) {
        int index = contentType.indexOf("boundary=");
        if (index != -1) {
            return contentType.substring(index + 9);
        }
        return null;
    }
}
