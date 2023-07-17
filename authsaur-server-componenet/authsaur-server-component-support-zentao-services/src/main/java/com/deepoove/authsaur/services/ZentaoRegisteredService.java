package com.deepoove.authsaur.services;

import org.apereo.cas.services.RegexRegisteredService;

public class ZentaoRegisteredService extends RegexRegisteredService {

    private String clientId;
    private String code;
    private String secret;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
