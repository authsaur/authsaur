package com.deepoove.authsaur.principal;

import lombok.Getter;

@Getter
public enum PrincipalAttr {

    AVATAR("avatar"),
    NAME("uname"),
    MAIL("umail"),
    PHONE("uphone"),
    PRINCIPAL("uprincipal"),
    USER_ID("actualid"),
    DEPT("udept"),
    AUTH_ID("uauthId"),
    AUTH_TYPE("uauthType"),
    RELATED("urelated");

    private String attr;

    PrincipalAttr(String type) {
        this.attr = type;
    }
}
