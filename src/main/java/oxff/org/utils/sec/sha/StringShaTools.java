package oxff.org.utils.sec.sha;

import org.apache.commons.codec.digest.DigestUtils;

public class StringShaTools {
    public static String sha1(String input){
        if (null == input || input.isEmpty()){
            return null;
        }
        return DigestUtils.sha1Hex(input);
    }

    public static String sha256(String input){
        if (null == input || input.isEmpty()){
            return null;
        }
        return DigestUtils.sha256Hex(input);
    }

    public static String sha512(String input){
        if (null == input || input.isEmpty()){
            return null;
        }
        return DigestUtils.sha512Hex(input);
    }
}
