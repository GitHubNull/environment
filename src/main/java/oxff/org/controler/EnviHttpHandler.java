package oxff.org.controler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import oxff.org.utils.requestProcessor.RequestProcessor;

import java.util.List;

public class EnviHttpHandler implements HttpHandler {

    MontoyaApi montoyaApi;
    Logging logger;
    private final RequestProcessor requestProcessor;

    public EnviHttpHandler(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        logger = montoyaApi.logging();
        requestProcessor = new RequestProcessor(logger);
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
        String pathWithoutQuery = requestProcessor.processPath(updateHttpRequestToBeSent.pathWithoutQuery());
        if (!pathWithoutQuery.equals(updateHttpRequestToBeSent.pathWithoutQuery())) {
            logger.logToOutput("pathWithoutQuery is changed, update");
            updateHttpRequestToBeSent = updateHttpRequestToBeSent.withPath(pathWithoutQuery);
        }

        // 处理查询参数
        String rawQuery = updateHttpRequestToBeSent.query();
        String query = requestProcessor.processQuery(updateHttpRequestToBeSent.query());
        if (!query.equals(rawQuery)) {
            logger.logToOutput("query is changed, update");
            String newPath = updateHttpRequestToBeSent.pathWithoutQuery() + "?" + query;
            updateHttpRequestToBeSent = updateHttpRequestToBeSent.withPath(newPath);
        }


        // 处理请求头， 如果有标记符，则处理，否则不处理
//        List<HttpHeader> resultHeaders = processHeaders(updateHttpRequestToBeSent.headers());
//        updateHttpRequestToBeSent = updateHttpRequestToBeSent.withUpdatedHeaders(resultHeaders);


        // 处理请求体
        ByteArray body = requestProcessor.processBody(updateHttpRequestToBeSent.body(), updateHttpRequestToBeSent.headers());
        if (null != body && body.length() > 0) {
            updateHttpRequestToBeSent = updateHttpRequestToBeSent.withBody(body);
        }

        return RequestToBeSentAction.continueWith(updateHttpRequestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        return ResponseReceivedAction.continueWith(httpResponseReceived);
    }
}
