package oxff.org.controler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.AutoUpdateType;
import oxff.org.model.VariableInfo;
import oxff.org.utils.RequestBodyProcessor;
import oxff.org.utils.Tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class EnviHttpHandler implements HttpHandler {

    MontoyaApi montoyaApi;
    Logging logger;

    public EnviHttpHandler(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        logger = montoyaApi.logging();
    }

    private static String processBodyArgs(List<VariableInfo> bodyVariables, String body) {
        for (VariableInfo bodyVariable : bodyVariables) {
            String variableName = bodyVariable.name;
            Arg arg = Environment.argsMap.get(variableName);
            if (null == arg || !arg.isEnabled()) {
                continue;
            }

            Method method = arg.getMethod();
            Script script = arg.getScript();
            if (null == method && null == script) {
                continue;
            }
            try {
                String oriBody = body;
                String newValue;
                AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
                if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                    if (null == script) {
                        continue;
                    }
                    Map<String, String> params = new HashMap<>();
                    params.put(arg.getName(), arg.getValue());
                    newValue = (String) Environment.groovyScripts.get(arg.getName()).invokeMethod(GROOVY_FUNCTION_NAME, params);
                } else {
                    if (null == method) {
                        continue;
                    }
                    if (Tools.needParams(autoUpdateType)) {
                        newValue = (String) method.invoke(arg.getLength());
                    } else {
                        newValue = (String) method.invoke(null);
                    }
                }

                if (newValue == null || newValue.isEmpty()) {
                    continue;
                }
                body = Tools.replaceSubstring(oriBody, bodyVariable.startIndex - 2, bodyVariable.endIndex + 2,
                                              newValue);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return body;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {
        logger.logToOutput("EnviHttpHandler.handleHttpRequestToBeSent()");
        List<ParsedHttpParameter> parsedHttpParameters = httpRequestToBeSent.parameters();

        // NOTE
        // 1. 先按照常规正常的http请求报文处理所有参数
        // 2. 如果有标记符，则处理，否则不处理直接返回
//        HttpRequest updateHttpRequestToBeSent = normalHttpProcess(httpRequestToBeSent);
        HttpRequest updateHttpRequestToBeSent = httpRequestToBeSent;


        // 处理路径
        String pathWithoutQuery = processPath(updateHttpRequestToBeSent.pathWithoutQuery());
        if (!pathWithoutQuery.equals(updateHttpRequestToBeSent.pathWithoutQuery())) {
            logger.logToOutput("pathWithoutQuery is changed, update");
            updateHttpRequestToBeSent = updateHttpRequestToBeSent.withPath(pathWithoutQuery);
        }

        // 处理查询参数
        String rawQuery = updateHttpRequestToBeSent.query();
        String query = processQuery(updateHttpRequestToBeSent.query());
        if (!query.equals(rawQuery)) {
            logger.logToOutput("query is changed, update");
            String newPath = updateHttpRequestToBeSent.pathWithoutQuery() + "?" + query;
            updateHttpRequestToBeSent = updateHttpRequestToBeSent.withPath(newPath);
        }


        // 处理请求头， 如果有标记符，则处理，否则不处理
        List<HttpHeader> resultHeaders = processHeaders(updateHttpRequestToBeSent.headers());
        updateHttpRequestToBeSent = updateHttpRequestToBeSent.withUpdatedHeaders(resultHeaders);


        // 处理请求体
        ByteArray body = processBody(updateHttpRequestToBeSent.body(), updateHttpRequestToBeSent.headers());
        if (null != body && body.length() > 0) {
            updateHttpRequestToBeSent = updateHttpRequestToBeSent.withBody(body);
        }

        return RequestToBeSentAction.continueWith(updateHttpRequestToBeSent);
    }

    private String processQuery(String query) {
        if (null == query || query.isBlank() || !query.contains("&")) {
            return query;
        }

        Map<String, String> queryMap = new HashMap<>();
        String[] queryArray = query.split("&");
        for (String queryItem : queryArray) {
            String[] queryItemArray = queryItem.split("=");
            String queryItemName = queryItemArray[0];
            String queryItemValue = queryItemArray[1];
            if (Tools.isMarker(queryItemValue)) {
                queryMap.put(queryItemName, queryItemValue);
                continue;
            }
            String queryItemNameWithoutMarker = Tools.stripMarker(queryItemName);
            Arg arg = Environment.argsMap.get(queryItemNameWithoutMarker);
            if (null == arg || !arg.isEnabled()) {
                logger.logToOutput("queryItemValue is not marker, skip");
                queryMap.put(queryItemName, queryItemValue);
                continue;
            }

            String newValue;
            AutoUpdateType autoUpdateType = arg.getAutoUpdateType();
            if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
                Script script = arg.getScript();
                if (null == script){
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
                if (Tools.needParams(autoUpdateType)) {
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

    private ByteArray processBody(ByteArray body, List<HttpHeader> headers) {
        if (null == body) {
            return null;
        }
        if (!Tools.isMarker(body)){
            return body;
        }

        byte[] result = RequestBodyProcessor.replacePlaceholders(body.getBytes(), headers);
        if (null == result || 0 == result.length){
            logger.logToOutput("replacePlaceholders return null");
            return body;
        }

        return ByteArray.byteArray(result);
    }

    private List<HttpHeader> processHeaders(List<HttpHeader> headers) {
        List<HttpHeader> result = new ArrayList<>();
        for (HttpHeader header : headers) {
            String headerName = header.name();
            String headerValue = header.value();
            if (Tools.isMarker(headerValue)) {
                result.add(header);
                continue;
            }

            String headerValueWithoutMarker = Tools.stripMarker(headerValue);
            Arg arg = Environment.argsMap.get(headerValueWithoutMarker);
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

    private String processPath(String pathWithoutQuery) {
        if (null == pathWithoutQuery || (Tools.isMarker(pathWithoutQuery) || pathWithoutQuery.strip().trim().isEmpty())) {
            return pathWithoutQuery;
        }

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

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        return ResponseReceivedAction.continueWith(httpResponseReceived);
    }
}
