package oxff.org.utils.requestProcessor;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.logging.Logging;
import oxff.org.utils.ArgTool;
import oxff.org.utils.requestProcessor.bodyProcessor.RequestBodyProcessor;
import oxff.org.utils.requestProcessor.headerProcessor.HeaderProcessor;
import oxff.org.utils.requestProcessor.urlPathProcessor.UrlPathProcessor;
import oxff.org.utils.requestProcessor.urlQueryProcessor.UrlQueryProcessor;

import java.util.List;


public class RequestProcessor {
    private final Logging logger;
    UrlPathProcessor urlPathProcessor;
    UrlQueryProcessor urlQueryProcessor;
    HeaderProcessor headerProcessor;

    public RequestProcessor(Logging logger) {
        this.logger = logger;
        urlPathProcessor = new UrlPathProcessor(logger);
        urlQueryProcessor = new UrlQueryProcessor(logger);
        headerProcessor = new HeaderProcessor(logger);
    }

    public String processPath(String pathWithoutQuery) {
        if (null == pathWithoutQuery || (!ArgTool.isMarker(pathWithoutQuery) || pathWithoutQuery.strip().trim().isEmpty())) {
            return pathWithoutQuery;
        }
        return urlPathProcessor.processPath(pathWithoutQuery);
    }

    public String processQuery(String query) {
        if (null == query || query.isBlank() || !query.contains("&")) {
            return query;
        }
        return urlQueryProcessor.processQuery(query);
    }

    public List<HttpHeader> processHeaders(List<HttpHeader> headers) {
        if (null == headers || headers.isEmpty()) {
            return headers;
        }
        return headerProcessor.processHeaders(headers);
    }

    public ByteArray processBody(ByteArray body, List<HttpHeader> headers) {
        if (null == body) {
            return null;
        }
        if (!ArgTool.isMarker(body)) {
            return body;
        }

        byte[] result = RequestBodyProcessor.replacePlaceholders(body.getBytes(), headers);
        if (null == result || 0 == result.length) {
            logger.logToOutput("replacePlaceholders return null");
            return body;
        }

        return ByteArray.byteArray(result);
    }
}
