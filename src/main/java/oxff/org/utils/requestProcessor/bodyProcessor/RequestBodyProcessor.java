package oxff.org.utils.requestProcessor.bodyProcessor;

import burp.api.montoya.http.message.HttpHeader;
import oxff.org.Environment;

import java.util.List;

public class RequestBodyProcessor {

    /**
     * 替换请求体中的占位符
     * 此方法根据请求的 Content-Type 处理请求体，支持 multipart/form-data、application/json、
     * application/xml 和 application/x-www-form-urlencoded 类型的请求体中占位符的替换
     * 如果 headers 或请求体为空，将直接返回原始请求体或 null
     *
     * @param requestBody 原始请求体字节数组
     * @param headers 请求的 HTTP 头信息列表
     * @return 替换占位符后的请求体字节数组，如果未进行替换则返回原始请求体
     */
    public static byte[] replacePlaceholders(byte[] requestBody, List<HttpHeader> headers) {
        // 检查 headers 是否为空
        if (headers == null || headers.isEmpty()) {
            // 如果 headers 为空，直接返回原始请求体
            return requestBody;
        }

        // 检查请求体是否为空
        if (requestBody == null || requestBody.length == 0) {
            // 如果请求体为空，直接返回 null
            return null;
        }

        try {
            // 从 headers 中提取 Content-Type
            String contentType = RequestBodyProcessorHelper.getContentType(headers);

            // 检查 Content-Type 是否为空
            if (contentType == null || contentType.isEmpty()) {
                // 如果 Content-Type 缺失，直接返回原始请求体
                return requestBody;
            }

            // 根据 Content-Type 类型进行分发
            if (contentType.startsWith("multipart/form-data")) {
                // 获取 boundary
                String boundary = RequestBodyProcessorHelper.getBoundary(contentType);
                // 检查 boundary 是否为空
                if (boundary == null) {
                    // 如果 boundary 缺失，返回原始请求体
                    return requestBody;
                }
                // 调用 MultipartBodyProcessor 进行占位符替换
                return MultipartBodyProcessor.replacePlaceholdersInMultipart(requestBody, boundary);
            } else if (contentType.startsWith("application/json")) {
                // 调用 TextBodyProcessor 进行 JSON 类型请求体的占位符替换
                return TextBodyProcessor.replacePlaceholdersInTextBody(requestBody);
            } else if (contentType.startsWith("application/xml")) {
                // 调用 TextBodyProcessor 进行 XML 类型请求体的占位符替换
                return TextBodyProcessor.replacePlaceholdersInTextBody(requestBody);
            } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
                // 调用 FormBodyProcessor 进行表单类型请求体的占位符替换
                return TextBodyProcessor.replacePlaceholdersInTextBody(requestBody);
            } else {
                // 如果 Content-Type 不支持，返回原始请求体
                return requestBody;
            }
        } catch (Exception e) {
            // 捕获所有异常，记录日志（如果需要），并返回原始请求体
            Environment.logger.logToError("Error replacing placeholders: " + e.getMessage());
            return requestBody;
        }
    }
}
