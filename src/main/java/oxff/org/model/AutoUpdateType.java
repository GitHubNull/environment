package oxff.org.model;

public enum AutoUpdateType {
    NONE,
    UUID,
    TIMESTAMP,
    SHA1_OF_TIMESTAMP,
    RANDOM_NUMBER,
    RANDOM_TEXT,
    INCREMENT_NUMBER,
    Groovy_CODE;

    public static AutoUpdateType getAutoUpdateType(String autoUpdateType) {
        return AutoUpdateType.valueOf(autoUpdateType);
    }
}
