package oxff.org.controler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.logging.Logging;
import oxff.org.Environment;

import java.util.List;

public class EnviHttpHandler implements HttpHandler {

    MontoyaApi montoyaApi;
    Logging logger;
    public EnviHttpHandler(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        logger = montoyaApi.logging();
    }
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {
        // 如果是纯字符型的请求报文体则处理，否则不处理
        if (!isTextRequestContentType(httpRequestToBeSent.headers())){
            return RequestToBeSentAction.continueWith(httpRequestToBeSent);
        }

        // 如果有标记符，则处理，否则不处理
        String body = httpRequestToBeSent.bodyToString();
        if (!body.contains(Environment.LEFT_MARKER) || !body.contains(Environment.RIGHT_MARKER)){
            return RequestToBeSentAction.continueWith(httpRequestToBeSent);
        }

        return RequestToBeSentAction.continueWith(httpRequestToBeSent);
    }

    private boolean isTextRequestContentType(List<HttpHeader> headers) {
        return headers.stream().anyMatch(header -> header.name().equalsIgnoreCase("Content-Type") &&
                header.value().contains("text") || header.value().contains("json") || header.value().contains("xml"));
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        return ResponseReceivedAction.continueWith(httpResponseReceived);
    }
}
