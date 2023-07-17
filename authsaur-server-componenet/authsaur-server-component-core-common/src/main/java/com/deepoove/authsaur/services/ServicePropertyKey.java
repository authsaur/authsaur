package com.deepoove.authsaur.services;

import lombok.Getter;

@Getter
public enum ServicePropertyKey {

    TYPE("type"),
    ALIAS("alias"),
    HOME_PAGE("homePage"),
    TAG("tag"),
    SAML_REQUEST("saml");

    private String attr;

    ServicePropertyKey(String type) {
        this.attr = type;
    }
}
