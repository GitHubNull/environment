package oxff.org.utils;

public class StringTool {
    public static boolean isPositiveInteger(String input) {
        return input.matches("^[1-9]\\d*$");
    }
}
