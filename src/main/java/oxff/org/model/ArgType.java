package oxff.org.model;

public enum ArgType {
    NUMBER,
    TEXT,
    ALL;

    public static ArgType getArgType(String argType) {
        return valueOf(argType.toUpperCase());
    }
}
