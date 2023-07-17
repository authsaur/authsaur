package com.deepoove.authsaur.rest.interceptor;

public class AuthHolder {
    private static final ThreadLocal<Object> securityUserHolder = new ThreadLocal<>();

    public static void resetUser() {
        securityUserHolder.remove();
    }

    public static <T> void setUser(T user) {
        if (user == null) {
            resetUser();
        } else {
            securityUserHolder.set(user);
        }
    }

    public static <T> T getUser() {
        return (T) securityUserHolder.get();
    }
}
