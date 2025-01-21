package oxff.org.utils.requestProcessor.urlPathProcessor;

import burp.api.montoya.logging.Logging;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.utils.Tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class UrlPathProcessor {
    private final Logging logger;

    public UrlPathProcessor(Logging logger) {
        this.logger = logger;
    }

    public String processPath(String pathWithoutQuery) {
        String[] pathSegments = pathWithoutQuery.split("/");
        logger.logToOutput("pathWithoutQuery: " + pathWithoutQuery);
        logger.logToOutput("pathSegments: " + pathSegments.length);
        if (pathSegments.length <= 1) {
            return pathWithoutQuery;
        }

        List<String> result = new ArrayList<>();

        for (String pathSegment : pathSegments) {
            if (Tools.isMarker(pathSegment)) {
                result.add(pathSegment);
                continue;
            }
            String pathSegmentWithoutMark = Tools.stripMarker(pathSegment);
            Arg arg = Environment.argsMap.get(pathSegmentWithoutMark);
            if (null == arg || !arg.isEnabled()) {
                result.add(pathSegment);
                continue;
            }
            String newValue;
            if (arg.getAutoUpdateType().equals(AutoUpdateType.Groovy_CODE)) {
                Script script = arg.getScript();
                if (null == script) {
                    logger.logToError("script is null");
                    result.add(pathSegment);
                    continue;
                }
                Map<String, String> params = new HashMap<>();
                params.put(arg.getName(), arg.getValue());

                try {
                    newValue = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                } catch (Exception e) {
                    logger.logToError(e);
                    result.add(pathSegment);
                    continue;
                }
            } else {
                Method method = arg.getMethod();
                if (null == method) {
                    logger.logToError("method is null");
                    result.add(pathSegment);
                    continue;
                }
                AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
                try {
                    if (Tools.needParams(autoUpdateType)) {
                        newValue = (String) method.invoke(null);
                    } else {
                        newValue = (String) method.invoke(null, arg.getValue());
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.logToError(e);
                    result.add(pathSegment);
                    continue;
                }
            }
            if (null == newValue) {
                logger.logToError("newValue is null");
                result.add(pathSegment);
            } else {
                logger.logToOutput("newValue: " + newValue);
                result.add(newValue);
            }
        }
        logger.logToOutput("new path: " + result);
        return String.join("/", result);
    }
}
