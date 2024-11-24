package oxff.org.model;

public class VariableInfo {
    public String name;
    public int startIndex;
    public int endIndex;

    @Override
    public String toString() {
        return "VariableInfo{" +
                "name='" + name + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }
}
