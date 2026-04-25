package oxff.org.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import oxff.org.model.Arg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlExporter {

    private static final String VERSION = "1.0";

    public static void exportToFile(List<Arg> args, File file) throws IOException {
        String yamlContent = exportToString(args);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(yamlContent);
        }
    }

    public static String exportToString(List<Arg> args) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", VERSION);
        root.put("exported_at", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        List<Map<String, Object>> argsList = new ArrayList<>();
        for (Arg arg : args) {
            Map<String, Object> argMap = new LinkedHashMap<>();
            argMap.put("id", arg.getId());
            argMap.put("name", arg.getName());
            argMap.put("type", arg.getType().toString());
            argMap.put("auto_update_type", arg.getAutoUpdateType().toString());
            argMap.put("length", arg.getLength());
            argMap.put("default_value", arg.getDefaultValue() != null ? arg.getDefaultValue() : "");
            argMap.put("value", arg.getValue() != null ? arg.getValue() : "");
            argMap.put("code_path", arg.getCodePath() != null ? arg.getCodePath() : "");
            argMap.put("enabled", arg.isEnabled());
            argMap.put("description", arg.getDescription() != null ? arg.getDescription() : "");
            argMap.put("persistent", arg.isPersistent());
            argsList.add(argMap);
        }
        root.put("args", argsList);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        Yaml yaml = new Yaml(options);
        return yaml.dump(root);
    }
}
