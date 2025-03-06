package oxff.org.utils.sec.sha;

import org.apache.commons.codec.digest.DigestUtils;

public class ByteArrayShaTools {
    public static String sha1(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return DigestUtils.sha1Hex(input);
    }

    public static String sha256(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return DigestUtils.sha256Hex(input);
    }

    public static String sha512(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return DigestUtils.sha512Hex(input);
    }
}
