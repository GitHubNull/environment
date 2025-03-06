package oxff.org.utils.requestProcessor.urlQueryProcessor;

import burp.api.montoya.logging.Logging;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.utils.ArgTool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class UrlQueryProcessor {
    private final Logging logger;

    public UrlQueryProcessor(Logging logger) {
        this.logger = logger;
    }

    public String processQuery(String query) {
        Map<String, String> queryMap = new HashMap<>();
        String[] queryArray = query.split("&");
        for (String queryItem : queryArray) {
            String[] queryItemArray = queryItem.split("=");
            String queryItemName = queryItemArray[0];
            String queryItemValue = queryItemArray[1];
            if (!ArgTool.isMarker(queryItemValue)) {
                queryMap.put(queryItemName, queryItemValue);
                continue;
            }
            String queryItemNameWithoutMarker = ArgTool.stripMarker(queryItemName);
            Arg arg = Environment.argTableModel.getArgByName(queryItemNameWithoutMarker);
            if (null == arg || !arg.isEnabled()) {
                logger.logToOutput("queryItemValue is not marker, skip");
                queryMap.put(queryItemName, queryItemValue);
                continue;
            }

            String newValue;
            AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
            if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                Script script = arg.getScript();
                if (null == script) {
                    continue;
                }
                Map<String, String> params = new HashMap<>();
                params.put(arg.getName(), arg.getValue());
                try {
                    newValue = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                } catch (Exception e) {
                    logger.logToError("groovy script error: " + e.getMessage());
                    newValue = arg.getValue();
                }

            } else {
                Method method = Environment.autoUpdateMethods.get(autoUpdateType);
                if (ArgTool.needParams(autoUpdateType)) {
                    try {
                        newValue = (String) method.invoke(arg.getLength());
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        logger.logToError("method invoke error: " + e.getMessage());
                        newValue = arg.getValue();
                    }

                } else {
                    try {
                        newValue = (String) method.invoke(null);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        logger.logToError("method invoke error: " + e.getMessage());
                        newValue = arg.getValue();
                    }
                }
            }
            queryMap.put(queryItemName, newValue);
        }
        return queryMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }
}
