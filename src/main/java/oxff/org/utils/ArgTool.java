package oxff.org.utils;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpHeader;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.GlobalConst;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.model.HeaderLineVariableInfo;
import oxff.org.model.VariableInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class ArgTool {
    public static String getNewValue(Arg arg) throws Exception {
        AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
        if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
            Script script = arg.getScript();
            if (script == null) {
                throw new IllegalArgumentException("script is null");
            }
            Map<String, String> params = new HashMap<>();
            params.put(arg.getName(), arg.getValue());
            try {
                @SuppressWarnings("UnnecessaryLocalVariable") String result = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                return result;
            } catch (Exception e) {
                throw new Exception("groovy script error: " + e.getMessage());
            }
        } else {
            Method method = Environment.autoUpdateMethods.get(autoUpdateType);
            if (needParams(autoUpdateType)) {
                return switch (autoUpdateType) {
                    case RANDOM_NUMBER, RANDOM_TEXT -> (String) method.invoke(null, arg.getLength());
                    case INCREMENT_NUMBER -> (String) method.invoke(null, arg.getValue());
                    default -> arg.getValue();
                };
            } else {
                return (String) method.invoke(null);
            }
        }
    }

    public static boolean needParams(AutoUpdateType autoUpdateType) {
        return !autoUpdateType.equals(AutoUpdateType.UUID) && !autoUpdateType.equals(AutoUpdateType.TIMESTAMP) &&
                !autoUpdateType.equals(AutoUpdateType.SHA1_OF_TIMESTAMP);
    }

    public static List<HeaderLineVariableInfo> extractHeadersVariables(List<HttpHeader> headers) {
        List<HeaderLineVariableInfo> variables = new ArrayList<>();
        // 正则表达式匹配前后最多有一个空格的变量
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        for (int i = 0; i < headers.size(); i++) {
            String headerValue = headers.get(i).value();
            Matcher matcher = pattern.matcher(headerValue);

            while (matcher.find()) {
                String match = matcher.group(0);
                String variableName = matcher.group(1).trim(); // Remove leading/trailing spaces

                // 检查前后空格的数量，确保最多只有一个空格
                if (match.startsWith("{{") && match.endsWith("}}")) {
                    HeaderLineVariableInfo variable = new HeaderLineVariableInfo();
                    variable.name = variableName;
                    variable.startIndex = matcher.start(1);
                    variable.endIndex = matcher.end(1);
                    variable.index = i;
                    variables.add(variable);
                }
            }
        }
        return variables;
    }

    public static VariableInfo extractBodyOneVariableInfo(String text) {
        // 正则表达式匹配前后最多有一个空格的变量
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String match = matcher.group(0);
            String variableName = matcher.group(1).strip().trim(); // Remove leading/trailing spaces

            // 检查前后空格的数量，确保最多只有一个空格
            if (match.startsWith("{{") && match.endsWith("}}")) {
                VariableInfo variable = new VariableInfo();
                variable.name = variableName;
                variable.startIndex = matcher.start(1);
                variable.endIndex = matcher.end(1);
                return variable;
            }
        }

        return null;
    }

    public static List<VariableInfo> extractBodyVariableInfos(String text) {
        List<VariableInfo> variables = new ArrayList<>();
        // 正则表达式匹配前后最多有一个空格的变量
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String match = matcher.group(0);
            String variableName = matcher.group(1).strip().trim(); // Remove leading/trailing spaces

            // 检查前后空格的数量，确保最多只有一个空格
            if (match.startsWith("{{ ") && match.endsWith(" }}") ||
                    match.startsWith("{{\"") && match.endsWith("\"}}") ||
                    match.startsWith("{{") && match.endsWith("}}")) {
                VariableInfo variable = new VariableInfo();
                variable.name = variableName;
                variable.startIndex = matcher.start(1);
                variable.endIndex = matcher.end(1);
                variables.add(variable);
            }
        }

        return variables;
    }

    public static String[] extractArgsFromRequestBodyByMark(String body) {
        // 使用正则表达式提取所有符合的分组
        return body.split(GlobalConst.LEFT_MARKER + ".*?" + GlobalConst.RIGHT_MARKER);
    }

    public static boolean isMarker(String input) {
        if (null == input || input.isBlank()) {
            return false;
        }
        return input.contains(GlobalConst.LEFT_MARKER) && input.contains(GlobalConst.RIGHT_MARKER);
    }

    public static boolean isMarker(ByteArray body) {
        if (null == body || 0 == body.length()) {
            return false;
        }
        return body.indexOf(GlobalConst.LEFT_MARKER) != -1 && body.indexOf(GlobalConst.RIGHT_MARKER) != -1;
    }

    public static boolean isMarker(List<HttpHeader> headers, String body) {
        return headers.stream().anyMatch(header -> header.value().contains(GlobalConst.LEFT_MARKER) &&
                header.value().contains(GlobalConst.RIGHT_MARKER)) || body.contains(GlobalConst.LEFT_MARKER) &&
                body.contains(GlobalConst.RIGHT_MARKER);
    }

    public static String stripMarker(String input) {
        return input.strip().trim().replace(GlobalConst.LEFT_MARKER, "").replace(GlobalConst.RIGHT_MARKER, "").strip()
                .trim();
    }

    public static boolean isTextRequestContentType(String httpMethod, List<HttpHeader> headers) {
        if (httpMethod.equalsIgnoreCase("GET")) {
            return true;
        }
        return headers.stream().anyMatch(header -> header.name().equalsIgnoreCase("Content-Type") &&
                header.value().contains("text") || header.value().contains("json") || header.value().contains("xml"));
    }

    public static boolean isTextRequestContentType(List<HttpHeader> headers) {
        return headers.stream().anyMatch(header -> header.name().equalsIgnoreCase("Content-Type") &&
                header.value().contains("text") || header.value().contains("json") || header.value().contains("xml"));
    }
}
