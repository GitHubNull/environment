package oxff.org.utils;

import oxff.org.Environment;
import oxff.org.model.Arg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static javax.xml.crypto.dsig.DigestMethod.SHA1;

public class Tools {
    public static String sha1(String input) {
        try {
            // 获取SHA-1的MessageDigest实例
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            // 对输入字符串进行加密
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            // 将字节转换为十六进制的字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha256(String input) {
        StringBuilder hexString;
        try {
            // 获取SHA-256的MessageDigest实例
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 对输入字符串进行加密
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            // 将字节转换为十六进制的字符串
            hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return hexString.toString();
    }


    public static String getRandomUUID()
    {
        return java.util.UUID.randomUUID().toString();
    }

    public static String getTimestamp()
    {
        return String.valueOf(System.currentTimeMillis());
    }

    public String getSha1OfTimestamp(){
        String timestamp = getTimestamp();
        return sha1(timestamp);
    }

    public static String getRandomNumber(int length)
    {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String getRandomText(int length)
    {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
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

    public static String[] extractArgsFromRequestBodyByMark(String bdoy){
        // 使用正则表达式提取所有符合的分组
        return bdoy.split(Environment.LEFT_MARKER + ".*?" + Environment.RIGHT_MARKER);
    }
}
