package oxff.org.utils;

import burp.api.montoya.http.message.HttpHeader;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.model.VariableInfo;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class RequestBodyProcessor {

    /**
     * 替换请求体中的占位符，根据传入的 Burp Suite Montoya API headers 中的 Content-Type 自动判断处理方式。
     * 如果出现异常，返回原始请求体。
     *
     * @param requestBody  请求体字节数组
     * @param headers      List<burp.api.montoya.http.message.HttpHeader> 类型的请求头
     * @return 替换后的请求体字节数组；如果出现异常，返回原始的请求体字节数组。
     */
    public static byte[] replacePlaceholders(byte[] requestBody, List<HttpHeader> headers) {
        if (headers == null || headers.isEmpty()) {
            // 如果 headers 为空，直接返回原始请求体
            return requestBody;
        }

        if (requestBody == null || requestBody.length == 0) {
            // 如果请求体为空，直接返回 null
            return null;
        }

        try {
            // 从 headers 中提取 Content-Type
            String contentType = getContentType(headers);

            if (contentType == null || contentType.isEmpty()) {
                // 如果 Content-Type 缺失，直接返回原始请求体
                return requestBody;
            }

            // 根据 Content-Type 类型进行分发
            if (contentType.startsWith("multipart/form-data")) {
                // 获取 boundary
                String boundary = getBoundary(contentType);
                if (boundary == null) {
                    // 如果 boundary 缺失，返回原始请求体
                    return requestBody;
                }
                return replacePlaceholdersInMultipart(requestBody, boundary);
            } else if (contentType.startsWith("application/json")) {
                return replacePlaceholdersInJson(requestBody);
            } else if (contentType.startsWith("application/xml")) {
                return replacePlaceholdersInXml(requestBody);
            } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
                return replacePlaceholdersInForm(requestBody);
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

    /**
     * 从 Burp Suite Montoya API 的 headers 中提取 Content-Type 值。
     */
    private static String getContentType(List<HttpHeader> headers) {
        for (HttpHeader header : headers) {
            if (header.name().equalsIgnoreCase("Content-Type")) {
                return header.value();
            }
        }
        return null; // 如果没有找到 Content-Type，返回 null
    }

    /**
     * 替换 multipart/form-data 请求体中的占位符。
     */
    private static byte[] replacePlaceholdersInMultipart(byte[] requestBodyBytes, String boundary) {
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] newLineBytes = "\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayBuilder result = new ByteArrayBuilder();

        int start = 0;
        while (true) {
            int boundaryIndex = indexOf(requestBodyBytes, boundaryBytes, start);
            if (boundaryIndex == -1) {
                result.append(requestBodyBytes, start, requestBodyBytes.length - start);
                break;
            }

            result.append(requestBodyBytes, start, boundaryIndex - start);
            result.append(boundaryBytes);

            start = boundaryIndex + boundaryBytes.length;

            if (start < requestBodyBytes.length && requestBodyBytes[start] == '-' && requestBodyBytes[start + 1] == '-') {
                result.append("--".getBytes(StandardCharsets.UTF_8));
                break;
            }

            int partEndIndex = indexOf(requestBodyBytes, newLineBytes, start);
            if (partEndIndex == -1) {
                return requestBodyBytes; // 如果格式不正确，返回原始请求体
            }

            String partHeaders = new String(requestBodyBytes, start, partEndIndex - start, StandardCharsets.UTF_8);
            start = partEndIndex + newLineBytes.length;

            if (partHeaders.contains("Content-Disposition") && !partHeaders.contains("filename")) {
                int contentEndIndex = indexOf(requestBodyBytes, boundaryBytes, start);
                if (contentEndIndex == -1) {
                    return requestBodyBytes; // 如果格式不正确，返回原始请求体
                }

                String content = new String(requestBodyBytes, start, contentEndIndex - start, StandardCharsets.UTF_8);
                List<VariableInfo> variables =Tools.extractBodyVariables(content);
                for (VariableInfo variable : variables) {
                    String name = variable.name;
                    String value = content.substring(variable.startIndex, variable.endIndex);

                    Arg arg = Environment.argsMap.get(name);
                    if (null == arg || !arg.isEnabled()) {
                        continue;
                    }
                    String newValue;
                    AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
                    if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                        Script script = arg.getScript();
                        if (script == null){
                            Environment.logger.logToError("script is null");
                            continue;
                        }
                        Map<String, String> params = new HashMap<>();
                        params.put(arg.getName(), arg.getValue());
                        try {
                            newValue = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                        } catch (Exception e) {
                            Environment.logger.logToError("groovy script error: " + e.getMessage());
                            newValue = arg.getValue();
                        }
                    }else{
                        Method method = Environment.autoUpdateMethods.get(autoUpdateType);
                        if (null == method){
                            Environment.logger.logToError("method is null");
                            continue;
                        }
                        if (Tools.needParams(autoUpdateType)) {
                            try {
                                newValue = (String) method.invoke(arg.getLength());
                            } catch (Exception e) {
                                Environment.logger.logToError("method invoke error: " + e.getMessage());
                                newValue = arg.getValue();
                            }
                        }else{
                            try {
                                newValue = (String) method.invoke(null);
                            } catch (Exception e) {
                                Environment.logger.logToError("method invoke error: " + e.getMessage());
                                newValue = arg.getValue();
                            }
                        }
                    }
                    content = Tools.replaceSubstring(content, variable.startIndex, variable.endIndex, newValue);
                }

                result.append(partHeaders.getBytes(StandardCharsets.UTF_8));
                result.append(newLineBytes);
                result.append(content.getBytes(StandardCharsets.UTF_8));
                result.append(newLineBytes);

                start = contentEndIndex;
            } else {
                int contentEndIndex = indexOf(requestBodyBytes, boundaryBytes, start);
                if (contentEndIndex == -1) {
                    return requestBodyBytes; // 如果格式不正确，返回原始请求体
                }

                result.append(partHeaders.getBytes(StandardCharsets.UTF_8));
                result.append(newLineBytes);
                result.append(requestBodyBytes, start, contentEndIndex - start);
                result.append(newLineBytes);

                start = contentEndIndex;
            }
        }

        return result.toByteArray();
    }

    /**
     * 替换 application/json 请求体中的占位符。
     */
    private static byte[] replacePlaceholdersInJson(byte[] requestBody) {
        String jsonString = new String(requestBody, StandardCharsets.UTF_8);
        List<VariableInfo> variables = Tools.extractBodyVariables(jsonString);
        for (VariableInfo variable : variables) {
            String name = variable.name;
            String value = jsonString.substring(variable.startIndex, variable.endIndex);
            Arg arg = Environment.argsMap.get(name);
            if (null == arg || !arg.isEnabled()) {
                continue;
            }
            String newValue;
            AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
            if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                Script script = arg.getScript();
                if (script == null){
                    Environment.logger.logToError("script is null");
                    continue;
                }
                Map<String, String> params = new HashMap<>();
                params.put(arg.getName(), arg.getValue());
                try {
                    newValue = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                } catch (Exception e) {
                    Environment.logger.logToError("groovy script error: " + e.getMessage());
                    newValue = arg.getValue();
                }
            }else {
                Method method = Environment.autoUpdateMethods.get(autoUpdateType);
                if (Tools.needParams(autoUpdateType)) {
                    try {
                        newValue = (String) method.invoke(arg.getLength());
                    } catch (Exception e) {
                        Environment.logger.logToError("method invoke error: " + e.getMessage());
                        newValue = arg.getValue();
                    }
                } else {
                    try {
                        newValue = (String) method.invoke(null);
                    }catch (Exception e){
                        Environment.logger.logToError("method invoke error: " + e.getMessage());
                        newValue = arg.getValue();
                    }
                }
            }
            jsonString = Tools.replaceSubstring(jsonString, variable.startIndex, variable.endIndex, newValue);
        }
        return jsonString.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 替换 application/xml 请求体中的占位符。
     */
    private static byte[] replacePlaceholdersInXml(byte[] requestBody) {
        String xmlString = new String(requestBody, StandardCharsets.UTF_8);
        List<VariableInfo> variables = Tools.extractBodyVariables(xmlString);
        for (VariableInfo variable : variables) {
            String name = variable.name;
            String value = xmlString.substring(variable.startIndex, variable.endIndex);
            Arg arg = Environment.argsMap.get(name);
            if (null == arg || !arg.isEnabled()) {
                continue;
            }
            String newValue = "";
            AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
            if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                Script script = arg.getScript();
                if (script == null){
                    Environment.logger.logToError("script is null");
                    continue;
                }
                Map<String, String> params = new HashMap<>();
                params.put(arg.getName(), arg.getValue());
                try {
                    newValue = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                } catch (Exception e) {
                    Environment.logger.logToError("groovy script error: " + e.getMessage());
                    newValue = arg.getValue();
                }
            }else {
                Method method = Environment.autoUpdateMethods.get(autoUpdateType);
                if (null == method){
                    Environment.logger.logToError("method is null");
                    continue;
                }
                if (Tools.needParams(autoUpdateType)) {
                    try {
                        newValue = (String) method.invoke(arg.getLength());
                    } catch (Exception e) {
                        Environment.logger.logToError("method invoke error: " + e.getMessage());
                        newValue = arg.getValue();
                    }
                } else {
                    try {
                        newValue = (String) method.invoke(null);
                    }catch (Exception e){
                        Environment.logger.logToError("method invoke error: " + e.getMessage());
                    }
                }
            }
            xmlString = Tools.replaceSubstring(xmlString, variable.startIndex, variable.endIndex, newValue);
        }
        return xmlString.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 替换 application/x-www-form-urlencoded 请求体中的占位符。
     */
    private static byte[] replacePlaceholdersInForm(byte[] requestBody) {
        String formString = new String(requestBody, StandardCharsets.UTF_8);
        try {
            String[] pairs = formString.split("&");
            Map<String, String> result = new LinkedHashMap<>();

            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length != 2) {
                    // 如果格式不正确，直接返回原始请求体
                    Environment.logger.logToError("Invalid form keyValue: " + Arrays.toString(keyValue) + " continue...");
                   return requestBody;
                }
                String key = URLDecoder.decode(keyValue[0].strip().trim(), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1].strip().trim(), StandardCharsets.UTF_8);
                if (Tools.isMarker(value)){
                    result.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
                    continue;
                }
                String valueWithoutMark = Tools.stripMarker(value);
                Arg arg = Environment.argsMap.get(valueWithoutMark);
                if (null == arg || !arg.isEnabled()) {
                    result.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
                    continue;
                }

                String newValue;
                AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
                if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                    Script script = arg.getScript();
                    if (script == null){
                        Environment.logger.logToError("script is null");
                        result.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
                        continue;
                    }
                    Map<String, String> params = new HashMap<>();
                    params.put(arg.getName(), arg.getValue());
                    try {
                        newValue = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                    } catch (Exception e) {
                        Environment.logger.logToError("groovy script error: " + e.getMessage());
                        newValue = arg.getValue();
                    }
                } else {
                    Method method = Environment.autoUpdateMethods.get(autoUpdateType);
                    if (null == method){
                        Environment.logger.logToError("method is null");
                        result.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
                        continue;
                    }
                    if (Tools.needParams(autoUpdateType)) {
                        try {
                            newValue = (String) method.invoke(arg.getLength());
                        } catch (Exception e) {
                            Environment.logger.logToError("method invoke error: " + e.getMessage());
                            newValue = arg.getValue();
                        }
                    } else {
                        try {
                            newValue = (String) method.invoke(null);
                        }catch (Exception e){
                            Environment.logger.logToError("method invoke error: "+ e.getMessage());
                            newValue = arg.getValue();
                        }
                    }
                }
                result.put(key, URLEncoder.encode(newValue, StandardCharsets.UTF_8));

            }
            return result.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"))
                    .getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 如果解析表单出错，返回原始请求体
            Environment.logger.logToError("Error parsing form: " + e.getMessage());
            return requestBody;
        }
    }

    /**
     * 从 Content-Type 中提取 boundary。
     */
    private static String getBoundary(String contentType) {
        int index = contentType.indexOf("boundary=");
        if (index != -1) {
            return contentType.substring(index + 9);
        }
        return null;
    }

    /**
     * 查找子数组在父数组中的索引。
     */
    private static int indexOf(byte[] array, byte[] target, int start) {
        outer:
        for (int i = start; i <= array.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static void main(String[] args) {
        // 示例调用
        byte[] requestBody = "{\"key\": \"{{id}}\", \"name\": \"{{name}}\"}".getBytes(StandardCharsets.UTF_8);
        List<HttpHeader> headers = List.of(
                HttpHeader.httpHeader("Content-Type", "application/json")
        );
        Map<String, String> replacements = Map.of("id", "12345", "name", "JohnDoe");

        byte[] modifiedRequestBody = replacePlaceholders(requestBody, headers);
        assert modifiedRequestBody != null;
        System.out.println(new String(modifiedRequestBody, StandardCharsets.UTF_8));
    }

    /**
     * 用于高效构建字节数组的工具类。
     */
    private static class ByteArrayBuilder {
        private byte[] buffer = new byte[1024];
        private int size = 0;

        public void append(byte[] data) {
            append(data, 0, data.length);
        }

        public void append(byte[] data, int offset, int length) {
            ensureCapacity(size + length);
            System.arraycopy(data, offset, buffer, size, length);
            size += length;
        }

        public byte[] toByteArray() {
            byte[] result = new byte[size];
            System.arraycopy(buffer, 0, result, 0, size);
            return result;
        }

        private void ensureCapacity(int capacity) {
            if (capacity > buffer.length) {
                byte[] newBuffer = new byte[Math.max(buffer.length * 2, capacity)];
                System.arraycopy(buffer, 0, newBuffer, 0, size);
                buffer = newBuffer;
            }
        }
    }
}
