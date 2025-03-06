package oxff.org.utils;

public class StringTool {
    public static boolean isPositiveInteger(String input) {
        return input.matches("^[1-9]\\d*$");
    }

    /**
     * 替换字符串中指定区间的子字符串。
     *
     * @param original 原始字符串
     * @param start    区间开始位置（包含）
     * @param end      区间结束位置（不包含）
     * @param replacement 要替换的新字符串
     * @return 替换后的字符串
     */
    public static String replaceSubstring(String original, int start, int end, String replacement) {
        if (original == null || replacement == null) {
            throw new IllegalArgumentException("Original string and replacement must not be null");
        }
        if (start < 0 || end > original.length() || start > end) {
            throw new StringIndexOutOfBoundsException("Invalid interval for start " + start + " and end " + end);
        }

        // 使用StringBuilder进行替换
        StringBuilder builder = new StringBuilder(original);
        builder.replace(start, end, replacement);

        return builder.toString();
    }
}
