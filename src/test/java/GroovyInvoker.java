import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GroovyInvoker {
    public static void main(String[] args) throws Exception {
        // 1. 加载Groovy脚本path/to/example.groovy
        GroovyClassLoader classLoader = new GroovyClassLoader();
        Class<?> groovyClass = classLoader.parseClass(new File("/Users/admin/workplace/devs/java-devs/grrovyTest/src/test/java/example.groovy"));

        // 2. 创建Groovy对象实例
        GroovyObject groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();

        // 3. 准备参数：Map<String, String>
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        // 4. 调用函数并传递参数
        Object result = groovyObject.invokeMethod("processData", params);

        System.out.println(result); // 输出：Processed data: value1, value2
    }
}
