package oxff.org.utils;

import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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

//    private String processIncrementNumber(String value) {
//        String newValue = Tools.getRandomIncrementNumber(value);
//        return newValue;
//    }
}
