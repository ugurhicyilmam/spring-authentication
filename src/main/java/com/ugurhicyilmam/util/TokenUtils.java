package com.ugurhicyilmam.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class TokenUtils {

    private static SecureRandom random = new SecureRandom();

    private TokenUtils() {
    }

    public static String generateToken() {
        int tokenLength = 32;
        return new BigInteger(tokenLength * 5, random).toString(32); // base32
    }

    public static String encodeBase64(String token) {
        return Base64.encodeBase64String(StringUtils.getBytesUtf8(token));
    }

    public static String decodeBase64(String token) {
        return StringUtils.newStringUtf8(Base64.decodeBase64(token));
    }

}
