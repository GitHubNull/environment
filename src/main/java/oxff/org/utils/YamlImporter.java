package oxff.org.utils;

import org.yaml.snakeyaml.Yaml;
import oxff.org.model.Arg;
import oxff.org.model.ArgType;
import oxff.org.model.AutoUpdateType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlImporter {

    @SuppressWarnings("unchecked")
    public static List<Arg> importFromFile(File file) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, Object> root = yaml.load(fis);
            return parseArgs(root);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Arg> importFromString(String yamlContent) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(yamlContent);
        return parseArgs(root);
    }

    @SuppressWarnings("unchecked")
    private static List<Arg> parseArgs(Map<String, Object> root) {
        List<Arg> args = new ArrayList<>();
        if (root == null || !root.containsKey("args")) {
            return args;
        }
        List<Map<String, Object>> argsList = (List<Map<String, Object>>) root.get("args");
        if (argsList == null) {
            return args;
        }
        for (Map<String, Object> argMap : argsList) {
            Arg arg = new Arg();
            arg.setId(toInt(argMap.get("id")));
            arg.setName(toString(argMap.get("name")));
            arg.setType(ArgType.getArgType(toString(argMap.get("type"), "ALL")));
            arg.setAutoUpdateType(AutoUpdateType.getAutoUpdateType(toString(argMap.get("auto_update_type"), "NONE")));
            arg.setLength(toInt(argMap.get("length")));
            arg.setDefaultValue(toString(argMap.get("default_value")));
            arg.setValue(toString(argMap.get("value")));
            arg.setCodePath(toString(argMap.get("code_path")));
            arg.setEnabled(toBoolean(argMap.get("enabled")));
            arg.setDescription(toString(argMap.get("description")));
            arg.setPersistent(toBoolean(argMap.get("persistent"), true));
            args.add(arg);
        }
        return args;
    }

    private static String toString(Object obj) {
        return toString(obj, "");
    }

    private static String toString(Object obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        return obj.toString();
    }

    private static int toInt(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private static boolean toBoolean(Object obj) {
        return toBoolean(obj, false);
    }

    private static boolean toBoolean(Object obj, boolean defaultValue) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return defaultValue;
    }
}
