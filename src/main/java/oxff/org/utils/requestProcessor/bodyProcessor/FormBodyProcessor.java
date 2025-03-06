package oxff.org.utils.requestProcessor.bodyProcessor;

import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.utils.ArgTool;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class FormBodyProcessor {
    /**
     * 替换 application/x-www-form-urlencoded 请求体中的占位符。
     */
    public static byte[] replacePlaceholdersInForm(byte[] requestBody) {
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
                if (!ArgTool.isMarker(value)) {
                    result.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
                    continue;
                }
                String valueWithoutMark = ArgTool.stripMarker(value);
                Arg arg = Environment.argTableModel.getArgByName(valueWithoutMark);
                if (null == arg || !arg.isEnabled()) {
                    result.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
                    continue;
                }

                String newValue;
                AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
                if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                    Script script = arg.getScript();
                    if (script == null) {
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
                    if (null == method) {
                        Environment.logger.logToError("method is null");
                        result.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
                        continue;
                    }
                    if (ArgTool.needParams(autoUpdateType)) {
                        try {
                            newValue = (String) method.invoke(arg.getLength());
                        } catch (Exception e) {
                            Environment.logger.logToError("method invoke error: " + e.getMessage());
                            newValue = arg.getValue();
                        }
                    } else {
                        try {
                            newValue = (String) method.invoke(null);
                        } catch (Exception e) {
                            Environment.logger.logToError("method invoke error: " + e.getMessage());
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
}
