package oxff.org.model;

public enum ArgDialogOpType {
    ADD,
    EDIT,
    DELETE,
    VIEW;

    public static ArgDialogOpType getArgDialogOpType(String argDialogOpType) {
        return ArgDialogOpType.valueOf(argDialogOpType.toUpperCase());
    }
}
