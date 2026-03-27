package com.example.demo.common;

import java.util.Random;

public class UsernameGenerator {
    private static final String PREFIX = "铲屎官";
    private static final String CHARS = "0123456789";
    private static final int SUFFIX_LENGTH = 7;
    private static final Random RANDOM = new Random();

    public static String generateRandomUsername() {
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            suffix.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return PREFIX + suffix.toString();
    }
}