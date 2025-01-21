package oxff.org.utils;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpHeader;
import oxff.org.GlobalConst;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.model.HeaderLineVariableInfo;
import oxff.org.model.VariableInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
    public static String sha1(String input) {
        try {
            // 获取SHA-1的MessageDigest实例
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            // 对输入字符串进行加密
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // 将字节转换为十六进制的字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha256(String input) {
        StringBuilder hexString;
        try {
            // 获取SHA-256的MessageDigest实例
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 对输入字符串进行加密
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // 将字节转换为十六进制的字符串
            hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return hexString.toString();
    }


    public static String getRandomUUID()
    {
        return java.util.UUID.randomUUID().toString();
    }

    public static String getTimestamp()
    {
        return String.valueOf(System.currentTimeMillis());
    }

    public static boolean isMarker(String input) {
        return !input.contains(GlobalConst.LEFT_MARKER) || !input.contains(GlobalConst.RIGHT_MARKER);
    }

    public static boolean isMarker(ByteArray body){
        if (null == body || 0 == body.length()){
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
                header.value().contains("text") || header.value().contains("json") || header.value() .contains("xml"));
    }

    public static String getSha1OfTimestamp(){
        String timestamp = getTimestamp();
        return sha1(timestamp);
    }

    public static String getRandomNumber(Integer length)
    {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String getRandomText(int length)
    {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String getRandomIncrementNumber(String srcNumber) {
        BigDecimal bd = new BigDecimal(srcNumber);
        return bd.add(BigDecimal.ONE).toString();
    }

    public static String generateByGroovy(Arg arg) throws IOException {
        String name = arg.getName();
        String srcValue = arg.getValue();
        String codePath = arg.getCodePath();
        return GroovyUtils.executeGroovyCode(codePath, name, srcValue);
    }

    public static String[] extractArgsFromRequestBodyByMark(String body){
        // 使用正则表达式提取所有符合的分组
        return body.split(GlobalConst.LEFT_MARKER + ".*?" + GlobalConst.RIGHT_MARKER);
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
                if (match.startsWith("{{") && match.endsWith("}}")){
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

    /**
     * 替换字符串中指定区间的子字符串。
     *
     * @param original 原始字符串
     * @param start    区间开始位置（包含）
     * @param end      区间结束位置（不包含）
     * @param replacement 要替换的新字符串
     * @return 替换后的字符串
     */
    public static String replaceSubstring(String original, int start, int end, String replacement) {
        if (original == null || replacement == null) {
            throw new IllegalArgumentException("Original string and replacement must not be null");
        }
        if (start < 0 || end > original.length() || start > end) {
            throw new StringIndexOutOfBoundsException("Invalid interval for start " + start + " and end " + end);
        }

        // 使用StringBuilder进行替换
        StringBuilder builder = new StringBuilder(original);
        builder.replace(start, end, replacement);

        return builder.toString();
    }

    public  static  boolean needParams(AutoUpdateType autoUpdateType){
        return !autoUpdateType.equals(AutoUpdateType.UUID) && !autoUpdateType.equals(AutoUpdateType.TIMESTAMP) &&
                !autoUpdateType.equals(AutoUpdateType.SHA1_OF_TIMESTAMP);
    }
}
