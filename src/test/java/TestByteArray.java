import burp.api.montoya.core.ByteArray;
import org.junit.Test;

public class TestByteArray {
    @Test
    public void testByteArrayFindSubByteArray() {
        ByteArray byteArray = ByteArray.byteArray("abcdefghijklmnopqrstuvwxyz");
        ByteArray subByteArray = ByteArray.byteArray("ijklmnopqrstuvwxyz");
        int index = byteArray.indexOf(subByteArray);
        System.out.println("Index: " + index);
    }
}
