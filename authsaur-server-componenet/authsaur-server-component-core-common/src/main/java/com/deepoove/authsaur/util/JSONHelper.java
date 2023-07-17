package com.deepoove.authsaur.util;

import com.google.gson.Gson;

import java.util.Map;

public final class JSONHelper {

    private static final Gson gson = new Gson();

    public static Map<String, Object> toMap(String str) {
        if (null == str) return null;
        return gson.fromJson(str, Map.class);
    }

    public static Map<String, Object> objToMap(Object obj) {
        return toMap(toStr(obj));
    }

    public static String toStr(Object obj) {
        if (null == obj) return null;
        return gson.toJson(obj);
    }

    public static <T> T fromStr(String str, Class<T> clazz) {
        if (null == str) return null;
        return gson.fromJson(str, clazz);
    }
}
