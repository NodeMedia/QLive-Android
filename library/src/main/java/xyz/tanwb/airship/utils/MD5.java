package xyz.tanwb.airship.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import xyz.tanwb.airship.BaseConstants;

public final class MD5 {

    private static final String MD5 = "MD5";

    private static final char[] HRXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5(File file) throws IOException {
        MessageDigest messagedigest;
        FileInputStream in = null;
        FileChannel ch = null;
        byte[] encodeBytes = null;
        try {
            messagedigest = MessageDigest.getInstance(MD5);
            in = new FileInputStream(file);
            ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest.update(byteBuffer);
            encodeBytes = messagedigest.digest();
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException(neverHappened);
        } finally {
            if (in != null) {
                in.close();
            }
            if (ch != null) {
                ch.close();
            }
        }
        return toHexString(encodeBytes);
    }

    public static String md5(String string) {
        byte[] encodeBytes;
        try {
            encodeBytes = MessageDigest.getInstance(MD5).digest(string.getBytes(BaseConstants.UTF8));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException neverHappened) {
            throw new RuntimeException(neverHappened);
        }
        return toHexString(encodeBytes);
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) return BaseConstants.NULL;
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(HRXDIGITS[(b >> 4) & 0x0F]);
            hex.append(HRXDIGITS[b & 0x0F]);
        }
        return hex.toString();
    }
}
