package oxff.org.model;

import java.util.Objects;

public class Arg {
    int id;
    String name;
    ArgType type;
    AutoUpdateType autoUpdateType;
    String defaultValue;
    String value;
    String codePath;
    String description;

    public Arg() {
    }

    public Arg(int id, String name, ArgType type, AutoUpdateType autoUpdateType, String defaultValue, String value, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.autoUpdateType = autoUpdateType;
        this.defaultValue = defaultValue;
        this.value = value;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArgType getType() {
        return type;
    }

    public void setType(ArgType type) {
        this.type = type;
    }

    public AutoUpdateType getAutoUpdateType() {
        return autoUpdateType;
    }

    public void setAutoUpdateType(AutoUpdateType autoUpdateType) {
        this.autoUpdateType = autoUpdateType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Arg{" + "id=" + id + ", name=" + name + ", type=" + type + ", autoUpdateType=" + autoUpdateType + ", defaultValue=" + defaultValue + ", value=" + value + ", description=" + description + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Arg)) {
            return false;
        }
        Arg arg = (Arg) o;
        return Objects.equals(name,
                              arg.name) && type == arg.type && autoUpdateType == arg.autoUpdateType && Objects.equals(
                defaultValue, arg.defaultValue) && Objects.equals(value, arg.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, autoUpdateType, defaultValue, value, description);
    }
}