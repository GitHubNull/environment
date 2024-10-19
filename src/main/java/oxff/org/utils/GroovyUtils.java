package oxff.org.utils;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class GroovyUtils {

    public static String getGroovyCode(String filePath) {
        // open file(filePath) and read content
        if (null == filePath || filePath.isEmpty() || filePath.trim().isEmpty()){
            return "";
        }
        return readFileContents(filePath);
    }

    public static String readFileContents(String filePath) {
        // open file(filePath) and read content
        if (null == filePath || filePath.isEmpty() || filePath.trim().isEmpty()){
            return "";
        }
        String content = "";
        try {
            // 使用Files类的readAllBytes方法读取文件的字节数据
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            // 使用新的字符串构造函数将字节数据转换为字符串
            content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static String executeGroovyCode(String codeFilePath, String name, String srcValue) throws IOException {
        // execute groovy code
        if (null == codeFilePath || codeFilePath.isEmpty() || codeFilePath.trim().isEmpty()){
            return "";
        }
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse(new File(codeFilePath));
        Map<String, String> args = new HashMap<>();
        args.put(name, srcValue);

        try{
            return (String)script.invokeMethod("modifyArg", args);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
