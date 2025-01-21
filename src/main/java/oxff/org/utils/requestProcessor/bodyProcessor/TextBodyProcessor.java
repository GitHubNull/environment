package oxff.org.utils.requestProcessor.bodyProcessor;

import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.model.VariableInfo;
import oxff.org.utils.Tools;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class TextBodyProcessor {
    public static byte[] replacePlaceholdersInTextBody(byte[] requestBody) {
        if (requestBody == null || requestBody.length == 0) {
            return requestBody;
        }

        String jsonString = new String(requestBody, StandardCharsets.UTF_8);
        VariableInfo variableInfo = Tools.extractBodyOneVariableInfo(jsonString);
        while (variableInfo != null) {
            try {
                String name = variableInfo.name;
                String value = jsonString.substring(variableInfo.startIndex, variableInfo.endIndex);
                Arg arg = Environment.argsMap.get(name);
                if (arg == null || !arg.isEnabled()) {
                    continue;
                }

                String newValue = getNewValue(arg);
                String replaceStr = "\\{\\{%s\\}\\}".formatted(name);
                jsonString = jsonString.replaceAll(replaceStr, newValue);

                // Update variableInfo for the next iteration
                variableInfo = Tools.extractBodyOneVariableInfo(jsonString);
            } catch (Exception e) {
                Environment.logger.logToError("Error processing placeholder: " + e.getMessage());
            }
        }

        return jsonString.getBytes(StandardCharsets.UTF_8);
    }

    public static String getNewValue(Arg arg) throws Exception {
        AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
        if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
            Script script = arg.getScript();
            if (script == null) {
                throw new IllegalArgumentException("script is null");
            }
            Map<String, String> params = new HashMap<>();
            params.put(arg.getName(), arg.getValue());
            return (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
        } else {
            Method method = Environment.autoUpdateMethods.get(autoUpdateType);
            if (Tools.needParams(autoUpdateType)) {
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
}
