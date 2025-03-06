package oxff.org.utils.sec.sha;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileShaTools {
    public static String sha1(String filePath) throws IOException {
        if (null == filePath || filePath.isEmpty() || filePath.trim().isEmpty()) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try (InputStream is = FileUtils.openInputStream(file)) {
            return DigestUtils.sha1Hex(is);
        }
    }

    public static String sha256(String filePath) throws IOException {
        if (null == filePath || filePath.isEmpty() || filePath.trim().isEmpty()) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try (InputStream is = FileUtils.openInputStream(file)) {
            return DigestUtils.sha256Hex(is);
        }
    }

    public static String sha512(String filePath) throws IOException {
        if (null == filePath || filePath.isEmpty() || filePath.trim().isEmpty()) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try (InputStream is = FileUtils.openInputStream(file)) {
            return DigestUtils.sha512Hex(is);
        }
    }
}
