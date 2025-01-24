package oxff.org.utils.requestProcessor.headerProcessor;

import burp.api.montoya.http.message.HttpHeader;
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

public class HeaderProcessor {
    private final Logging logger;

    public HeaderProcessor(Logging logger) {
        this.logger = logger;
    }

    public List<HttpHeader> processHeaders(List<HttpHeader> headers) {
        List<HttpHeader> result = new ArrayList<>();
        for (HttpHeader header : headers) {
            String headerName = header.name();
            String headerValue = header.value();
            if (!Tools.isMarker(headerValue)) {
                result.add(header);
                continue;
            }

            String headerValueWithoutMarker = Tools.stripMarker(headerValue);
            Arg arg = Environment.argTableModel.getArgByName(headerValueWithoutMarker);
            if (null == arg || !arg.isEnabled()) {
                result.add(header);
                continue;
            }
            String newValue;
            if (arg.getAutoUpdateType().equals(AutoUpdateType.Groovy_CODE)) {
                Script script = arg.getScript();
                if (null == script) {
                    logger.logToError("script is null");
                    result.add(header);
                    continue;
                }
                Map<String, String> params = new HashMap<>();
                params.put(arg.getName(), arg.getValue());
                try {
                    newValue = (String) script.invokeMethod(GROOVY_FUNCTION_NAME, params);
                } catch (Exception e) {
                    logger.logToError(e);
                    result.add(header);
                    continue;
                }
            } else {
                Method method = arg.getMethod();
                AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
                try {
                    if (Tools.needParams(autoUpdateType)) {
                        newValue = (String) method.invoke(null, arg.getValue());
                    } else {
                        newValue = (String) method.invoke(null);
                    }

                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.logToError(e);
                    result.add(header);
                    continue;
                }
            }
            if (null == newValue) {
                logger.logToError("newValue is null");
                result.add(header);
            } else {
                logger.logToOutput("newValue: " + newValue);
                result.add(HttpHeader.httpHeader(headerName, newValue));
            }
        }
        return result;
    }
}
