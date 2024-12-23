package com.crazykid.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author arthur
 * @date 2024/12/23 18:20
 */
public class Md5Utils {
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f' };

    private Md5Utils() {
    }

    /**
     * 通过给定字符串生成md5值
     *
     * @param content
     * @return
     */
    public static String getMD5String(String content) {

        return getMD5String(content, Charset.defaultCharset());

    }

    public static String getMD5String(String content, Charset charset) {
        byte[] btInput = content.getBytes(charset);
        //获得MD5摘要算法的 MessageDigest 对象
        MessageDigest mdInst = null;
        try {
            mdInst = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }
        //使用指定的字节更新摘要
        mdInst.update(btInput);
        //获得密文
        byte[] md = mdInst.digest();
        //把密文转换成十六进制的字符串形式
        int j = md.length;
        char[] str = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = md[i];
            str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
            str[k++] = HEX_DIGITS[byte0 & 0xf];
        }
        return new String(str);
    }
}
