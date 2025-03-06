package oxff.org.utils;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.utils.sec.sha.FileShaTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static oxff.org.Environment.GROOVY_FUNCTION_NAME;

public class GroovyUtils {
    public final static Map<String, Script> groovyScripts = new ConcurrentHashMap<>();

    public static String getGroovyCode(String filePath) {
        // open file(filePath) and read content
        if (null == filePath || filePath.isEmpty() || filePath.trim().isEmpty()) {
            return "";
        }
        return readFileContents(filePath);
    }

    public static String readFileContents(String filePath) {
        // open file(filePath) and read content
        if (null == filePath || filePath.isEmpty() || filePath.trim().isEmpty()) {
            return "";
        }
        String content = "";
        try {
            // 使用Files类的readAllBytes方法读取文件的字节数据
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            // 使用新的字符串构造函数将字节数据转换为字符串
            content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            Environment.logger.logToError("Error reading file: " + e.getMessage());
        }
        return content;
    }

    public static String executeGroovyCode(String codeFilePath, String name, String oriValue) throws IOException {
        // execute groovy code
        if (null == codeFilePath || codeFilePath.isEmpty() || codeFilePath.trim().isEmpty()) {
            return "";
        }
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse(new File(codeFilePath));
        Map<String, String> args = new HashMap<>();
        args.put(name, oriValue);

        try {
            return (String) script.invokeMethod("modifyArg", args);
        } catch (Exception e) {
            Environment.logger.logToError("Error executing groovy code: " + e.getMessage());
            return oriValue;
        }
    }

    public static Script getScript(String codeFilePath) throws IOException {
        File codeFile = new File(codeFilePath);
        //noinspection ConstantValue
        if (null == codeFilePath || codeFilePath.trim().isEmpty() || !codeFile.exists() || !codeFile.isFile() ||
                !codeFilePath.endsWith(".groovy")) {
            Environment.logger.logToError("Error executing groovy code: codeFilePath is null or empty or not a file or not exists");
            return null;
        }
        String hash = null;

        try {
            hash = FileShaTools.sha1(codeFilePath);
        } catch (IOException e) {
            Environment.logger.logToError("Error executing groovy code: " + e.getMessage());
            return null;
        }

        if (hash == null){
            Environment.logger.logToError("Error executing groovy code: hash is null");
            return null;
        }

        if (groovyScripts.containsKey(hash)) {
            Environment.logger.logToError("Error executing groovy code: script already exists");
            return groovyScripts.get(hash);
        }

        GroovyShell shell = new GroovyShell();

        Script script;
        try {
            script = shell.parse(new File(codeFilePath));
        } catch (Exception e) {
            Environment.logger.logToError("Error executing groovy code: " + e.getMessage());
            return null;
        }
        if (null == script) {
            Environment.logger.logToError("Error executing groovy code: script is null");
            return null;
        }

        Set<String> methodNames = getScriptMethodNames(script);
        if (!methodNames.contains(GROOVY_FUNCTION_NAME)) {
            Environment.logger.logToError("Error executing groovy code: script does not contain a method named modify");
            return null;
        }

        groovyScripts.put(hash, script);

        return script;
    }

    public static Set<String> getScriptMethodNames(Script script) {
        if (null == script) {
            return null;
        }
        Set<String> result = new HashSet<>();
        for (java.lang.reflect.Method method : script.getClass().getMethods()) {
            result.add(method.getName());
        }
        return result;
    }
}
