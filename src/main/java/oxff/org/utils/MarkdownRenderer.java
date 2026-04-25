package oxff.org.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownRenderer {

    private static final String HTML_TEMPLATE = """
            <html>
            <head>
            <style>
            body { font-family: 'Microsoft YaHei', Arial, sans-serif; font-size: 13px; line-height: 1.6; color: #333; margin: 16px; }
            h1 { font-size: 20px; border-bottom: 2px solid #ff6600; padding-bottom: 6px; margin-top: 24px; }
            h2 { font-size: 17px; border-bottom: 1px solid #ddd; padding-bottom: 4px; margin-top: 20px; }
            h3 { font-size: 15px; margin-top: 16px; }
            h4 { font-size: 14px; }
            h5 { font-size: 13px; }
            h6 { font-size: 12px; color: #666; }
            code { background: #f4f4f4; padding: 2px 5px; border-radius: 3px; font-family: Consolas, monospace; font-size: 12px; }
            pre { background: #f4f4f4; padding: 12px; border-radius: 4px; overflow-x: auto; }
            pre code { background: none; padding: 0; }
            table { border-collapse: collapse; width: 100%; margin: 12px 0; }
            th { background: #ff6600; color: white; padding: 8px 12px; text-align: left; font-weight: bold; }
            td { border: 1px solid #ddd; padding: 8px 12px; }
            tr:nth-child(even) td { background: #f9f9f9; }
            hr { border: none; border-top: 1px solid #ddd; margin: 20px 0; }
            a { color: #ff6600; text-decoration: none; }
            a:hover { text-decoration: underline; }
            ul, ol { padding-left: 24px; }
            li { margin: 4px 0; }
            strong { color: #222; }
            blockquote { border-left: 4px solid #ff6600; padding-left: 12px; color: #555; margin: 12px 0; }
            </style>
            </head>
            <body>
            %s
            </body>
            </html>
            """;

    public static String render(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return HTML_TEMPLATE.replace("%s", "");
        }
        return HTML_TEMPLATE.replace("%s", convertToHtml(markdown));
    }

    private static String convertToHtml(String md) {
        // Escape HTML entities first
        String html = escapeHtml(md);

        // Extract code blocks with placeholder
        StringBuilder codeBlocks = new StringBuilder();
        Pattern codeBlockPattern = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
        Matcher codeBlockMatcher = codeBlockPattern.matcher(html);
        int idx = 0;
        while (codeBlockMatcher.find()) {
            String lang = codeBlockMatcher.group(1);
            String code = codeBlockMatcher.group(2);
            codeBlocks.append("<pre><code>").append(code.trim()).append("</code></pre>\n");
            html = html.replace(codeBlockMatcher.group(), "%%CODEBLOCK_" + idx + "%%");
            idx++;
        }

        // Inline code
        html = html.replaceAll("(?<!`)`([^`]+)`(?!`)", "<code>$1</code>");

        // Headings (must be before bold/italic)
        html = html.replaceAll("(?m)^###### (.+)$", "<h6>$1</h6>");
        html = html.replaceAll("(?m)^##### (.+)$", "<h5>$1</h5>");
        html = html.replaceAll("(?m)^#### (.+)$", "<h4>$1</h4>");
        html = html.replaceAll("(?m)^### (.+)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^## (.+)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^# (.+)$", "<h1>$1</h1>");

        // Horizontal rule
        html = html.replaceAll("(?m)^---+$", "<hr>");

        // Bold and italic
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
        html = html.replaceAll("\\*(.+?)\\*", "<em>$1</em>");

        // Links
        html = html.replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "<a href=\"$2\">$1</a>");

        // Tables
        html = convertTables(html);

        // Unordered lists
        html = convertUnorderedLists(html);

        // Ordered lists
        html = convertOrderedLists(html);

        // Paragraphs: wrap lines separated by blank lines in <p> tags
        html = convertParagraphs(html);

        // Restore code blocks
        for (int i = 0; i < idx; i++) {
            String[] parts = codeBlocks.toString().split("\n", -1);
            // Rebuild code blocks properly
            html = html.replace("%%CODEBLOCK_" + i + "%%", getCodeBlockByIndex(codeBlocks.toString(), i));
        }

        // Clean up multiple blank lines
        html = html.replaceAll("\\n{3,}", "\n\n");

        return html.trim();
    }

    private static String getCodeBlockByIndex(String allBlocks, int targetIdx) {
        String[] blocks = allBlocks.split("(?=<pre>)");
        for (String block : blocks) {
            if (block.startsWith("<pre>") && targetIdx-- <= 0) {
                return block;
            }
        }
        return "";
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String convertTables(String html) {
        // Match table rows: lines starting and ending with |
        String[] lines = html.split("\n");
        StringBuilder result = new StringBuilder();
        boolean inTable = false;
        boolean headerRow = true;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                if (!inTable) {
                    result.append("<table>\n");
                    inTable = true;
                    headerRow = true;
                }
                if (trimmed.matches("\\|[\\s\\-:]+\\|")) {
                    // Separator row, skip
                    headerRow = false;
                    continue;
                }
                String[] cells = trimmed.split("\\|");
                StringBuilder row = new StringBuilder("<tr>\n");
                for (int i = 1; i < cells.length; i++) { // skip first empty split
                    String cell = cells[i].trim();
                    if (headerRow) {
                        row.append("<th>").append(cell).append("</th>\n");
                    } else {
                        row.append("<td>").append(cell).append("</td>\n");
                    }
                }
                row.append("</tr>\n");
                result.append(row);
                headerRow = false;
            } else {
                if (inTable) {
                    result.append("</table>\n");
                    inTable = false;
                }
                result.append(line).append("\n");
            }
        }
        if (inTable) {
            result.append("</table>\n");
        }
        return result.toString();
    }

    private static String convertUnorderedLists(String html) {
        // Lines starting with "- " or "* "
        String[] lines = html.split("\n");
        StringBuilder result = new StringBuilder();
        boolean inList = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^[-*] .+")) {
                if (!inList) {
                    result.append("<ul>\n");
                    inList = true;
                }
                String content = trimmed.substring(2);
                result.append("<li>").append(content).append("</li>\n");
            } else {
                if (inList) {
                    result.append("</ul>\n");
                    inList = false;
                }
                result.append(line).append("\n");
            }
        }
        if (inList) {
            result.append("</ul>\n");
        }
        return result.toString();
    }

    private static String convertOrderedLists(String html) {
        String[] lines = html.split("\n");
        StringBuilder result = new StringBuilder();
        boolean inList = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^\\d+\\. .+")) {
                if (!inList) {
                    result.append("<ol>\n");
                    inList = true;
                }
                String content = trimmed.replaceFirst("^\\d+\\. ", "");
                result.append("<li>").append(content).append("</li>\n");
            } else {
                if (inList) {
                    result.append("</ol>\n");
                    inList = false;
                }
                result.append(line).append("\n");
            }
        }
        if (inList) {
            result.append("</ol>\n");
        }
        return result.toString();
    }

    private static String convertParagraphs(String html) {
        String[] lines = html.split("\n");
        StringBuilder result = new StringBuilder();
        StringBuilder paragraph = new StringBuilder();
        boolean inPre = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("<pre>")) {
                flushParagraph(result, paragraph);
                result.append(line).append("\n");
                inPre = true;
                continue;
            }
            if (trimmed.equals("</pre>")) {
                result.append(line).append("\n");
                inPre = false;
                continue;
            }
            if (inPre) {
                result.append(line).append("\n");
                continue;
            }

            // Skip lines that are already HTML elements
            if (trimmed.matches("^<(h[1-6]|table|/table|tr|/tr|th|th|td|td|ul|/ul|ol|/ol|li|/li|hr|p).*")) {
                flushParagraph(result, paragraph);
                result.append(line).append("\n");
                continue;
            }

            if (trimmed.isEmpty()) {
                flushParagraph(result, paragraph);
                continue;
            }

            paragraph.append(trimmed).append(" ");
        }
        flushParagraph(result, paragraph);

        return result.toString();
    }

    private static void flushParagraph(StringBuilder result, StringBuilder paragraph) {
        if (!paragraph.isEmpty()) {
            String content = paragraph.toString().trim();
            if (!content.startsWith("<")) {
                result.append("<p>").append(content).append("</p>\n");
            } else {
                result.append(content).append("\n");
            }
            paragraph.setLength(0);
        }
    }
}
