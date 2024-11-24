import burp.api.montoya.core.ByteArray;
import org.junit.Test;
import oxff.org.model.VariableInfo;
import oxff.org.utils.Tools;

import java.util.List;

import static oxff.org.utils.Tools.extractBodyVariables;

public class TestTools {
    @Test
    public void testExtractVariables(){
        String httpRawRequest = "GET /api/resource/{{ id }} HTTP/1.1\r\nHost: {{host}}\r\n\r\n";
        List<VariableInfo> variables = extractBodyVariables(httpRawRequest);
        System.out.println("Variables len:%d".formatted(variables.size()));
        for (VariableInfo variable : variables) {
            System.out.println("Variable Name: " + variable.name);
            System.out.println("Start Index: " + variable.startIndex);
            System.out.println("End Index: " + variable.endIndex);
            System.out.println();
        }
    }

    @Test
    public void testMarker(){
        String body = "{{id}}";
        ByteArray bodyByteArray = ByteArray.byteArray(body);
        System.out.println("Marker: " + Tools.isMarker(bodyByteArray));
    }

}
