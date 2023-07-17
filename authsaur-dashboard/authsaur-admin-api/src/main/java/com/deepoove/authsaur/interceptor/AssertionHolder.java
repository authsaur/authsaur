package com.deepoove.authsaur.interceptor;

import com.deepoove.authsaur.model.CasingUser;

public class AssertionHolder {
    private static final ThreadLocal<CasingUser> threadLocal = new ThreadLocal();

    public AssertionHolder() {
    }

    public static CasingUser getAssertion() {
        return threadLocal.get();
    }

    public static void setAssertion(CasingUser assertion) {
        threadLocal.set(assertion);
    }

    public static void clear() {
        threadLocal.remove();
    }
}
