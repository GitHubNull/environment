package oxff.org.utils.requestProcessor.bodyProcessor;

import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.ArgType;
import oxff.org.model.AutoUpdateType;
import oxff.org.model.VariableInfo;
import oxff.org.utils.Tools;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static oxff.org.Environment.*;

public class MultipartBodyProcessor {
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

    /**
     * 替换 multipart/form-data 请求体中的占位符。
     */
    public static byte[] replacePlaceholdersInMultipart(byte[] requestBodyBytes, String boundary) {
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] newLineBytes = "\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayBuilder result = new ByteArrayBuilder();

        HashMap<String, Arg> processedArgs = new HashMap<>();
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
                VariableInfo variableInfo = Tools.extractBodyOneVariableInfo(content);
                while (null != variableInfo) {
                    String name = variableInfo.name;
                    String value = content.substring(variableInfo.startIndex, variableInfo.endIndex);

                    Arg arg = Environment.argsMap.get(name);
                    if (null == arg || !arg.isEnabled()) {
                        continue;
                    }
                    String newValue;
                    AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
                    if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                        Script script = arg.getScript();
                        if (script == null) {
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
                    } else {
                        Method method = Environment.autoUpdateMethods.get(autoUpdateType);
                        if (null == method) {
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
                            } catch (Exception e) {
                                Environment.logger.logToError("method invoke error: " + e.getMessage());
                                newValue = arg.getValue();
                            }
                        }
                    }
                    String replaceStr = "\\{\\{" + name + "\\}\\}";
                    content = content.replaceAll(replaceStr, newValue);
                    variableInfo = Tools.extractBodyOneVariableInfo(content);
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
}
