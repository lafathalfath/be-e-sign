package org.bh_foundation.e_sign.utils;

import java.security.SecureRandom;

public class RandomStringUtils {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(Integer length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer i = 0; i < length; i++) {
            Integer index = RANDOM.nextInt(ALPHABET.length());
            stringBuilder.append(ALPHABET.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static String generateUpperCase(Integer length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer i = 0; i < length; i++) {
            Integer index = RANDOM.nextInt(UPPER_CASE.length());
            stringBuilder.append(UPPER_CASE.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static String generateLowerCase(Integer length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer i = 0; i < length; i++) {
            Integer index = RANDOM.nextInt(LOWER_CASE.length());
            stringBuilder.append(LOWER_CASE.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
