package oxff.org.utils.requestProcessor.bodyProcessor;

import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.model.VariableInfo;
import oxff.org.utils.ArgTool;
import oxff.org.utils.Tools;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class TextBodyProcessor {
    public static byte[] replacePlaceholdersInTextBody(byte[] requestBody) {
        if (requestBody == null || requestBody.length == 0) {
            return requestBody;
        }

        String jsonString = new String(requestBody, StandardCharsets.UTF_8);
        VariableInfo variableInfo = Tools.extractBodyOneVariableInfo(jsonString);
        HashMap<String, String> nullOrDisabledArgs = new HashMap<>();
        int nullOrDisabledArgsCount = 0;
        while (variableInfo != null) {
            String newValue;
            String name = variableInfo.name;
            String value = jsonString.substring(variableInfo.startIndex, variableInfo.endIndex);
            Arg arg = Environment.argTableModel.getArgByName(name);
            if (arg == null || !arg.isEnabled()) {
                Environment.logger.logToError("Error processing placeholder: " + name);
                String replaceStr = "\\{\\{%s\\}\\}".formatted(name);
                newValue = "@@__NDA_%d__@@".formatted(nullOrDisabledArgsCount);
                nullOrDisabledArgsCount++;
                nullOrDisabledArgs.put(newValue, "{{%s}}".formatted(name));
                jsonString = jsonString.replaceAll(replaceStr, newValue);
                variableInfo = Tools.extractBodyOneVariableInfo(jsonString);
                continue;
            }
            try {
                newValue = ArgTool.getNewValue(arg);

            } catch (Exception e) {
                Environment.logger.logToError("Error processing placeholder: " + e.getMessage());
                newValue = value;
            }

            if (AutoUpdateType.INCREMENT_NUMBER.equals(arg.getAutoUpdateType()) && !value.equals(newValue)){
                arg.setValue(newValue);
//                Environment.argsMap.put(name, arg);
                Environment.argTableModel.updateArgById(arg.getId(), arg);
            }

            String replaceStr = "\\{\\{%s\\}\\}".formatted(name);
            jsonString = jsonString.replaceAll(replaceStr, newValue);

            // Update variableInfo for the next iteration
            variableInfo = Tools.extractBodyOneVariableInfo(jsonString);
        }

        nullOrDisabledArgsCount = 0;
        for (String key : nullOrDisabledArgs.keySet()) {
            String replaceStr = "@@__NDA_%d__@@".formatted(nullOrDisabledArgsCount);
            jsonString = jsonString.replaceAll(replaceStr, nullOrDisabledArgs.get(key));
            nullOrDisabledArgsCount++;
        }

        return jsonString.getBytes(StandardCharsets.UTF_8);
    }
}
