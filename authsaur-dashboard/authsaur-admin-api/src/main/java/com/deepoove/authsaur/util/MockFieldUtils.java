package com.deepoove.authsaur.util;

public class MockFieldUtils {

    public static String mockField(String value) {
        if (value.length() >= 6) {
            return "********" + value.substring(value.length() - 4, value.length());
        }
        return "********";
    }

    public static boolean isMockField(String value) {
        return value.startsWith("********");
    }
}
