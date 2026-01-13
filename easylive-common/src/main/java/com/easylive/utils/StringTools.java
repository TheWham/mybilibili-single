package com.easylive.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {
    public static String generateRandomNumber(int count) {
        return RandomStringUtils.random(count, false, true);
    }

    public static boolean isEmpty(String value) {
        if (null == value || "".equals(value) || "null".equals(value) || "\u0000".equals(value))
            return true;
        else
            return "".equals(value.trim());
    }

    public static String md5Password(String password) {
        return isEmpty(password) ? null : DigestUtils.md5Hex(password);
    }

    public static String generateRandomStr(int length5) {
        return RandomStringUtils.random(length5, true, true);
    }
}