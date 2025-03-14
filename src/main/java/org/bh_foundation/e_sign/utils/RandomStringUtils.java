package org.bh_foundation.e_sign.utils;

import java.security.SecureRandom;

public class RandomStringUtils {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(Integer length) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Integer i = 0; i < length; i++) {
            Integer index = RANDOM.nextInt(ALPHABET.length());
            stringBuilder.append(ALPHABET.charAt(index));
        }

        return stringBuilder.toString();
    }
}
