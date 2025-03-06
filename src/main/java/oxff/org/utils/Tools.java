package oxff.org.utils;

import oxff.org.model.Arg;
import oxff.org.utils.sec.sha.StringShaTools;

import java.io.IOException;
import java.math.BigDecimal;

public class Tools {

    public static String getRandomUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    public static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }


    public static String getSha1OfTimestamp() {
        String timestamp = getTimestamp();
        return StringShaTools.sha1(timestamp);
    }

    public static String getRandomNumber(Integer length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String getRandomText(int length) {
        @SuppressWarnings("SpellCheckingInspection") String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String getRandomIncrementNumber(String srcNumber) {
        BigDecimal bd = new BigDecimal(srcNumber);
        return bd.add(BigDecimal.ONE).toString();
    }

    public static String generateByGroovy(Arg arg) throws IOException {
        String name = arg.getName();
        String srcValue = arg.getValue();
        String codePath = arg.getCodePath();
        return GroovyUtils.executeGroovyCode(codePath, name, srcValue);
    }

}
