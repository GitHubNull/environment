package oxff.org.model;

public enum ArgType {
    NUMBER,
    TEXT;

    public static ArgType getArgType(String argType) {
        return valueOf(argType.toUpperCase());
    }
}
