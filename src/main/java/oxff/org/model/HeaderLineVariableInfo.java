package oxff.org.model;

public class HeaderLineVariableInfo {
    public int index;
    public String name;
    public int startIndex;
    public int endIndex;

    @Override
    public String toString() {
        return "HeaderLineVariableInfo{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
}
}
