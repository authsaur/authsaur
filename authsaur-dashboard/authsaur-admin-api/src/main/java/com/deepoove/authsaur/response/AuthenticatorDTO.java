package com.deepoove.authsaur.response;

import lombok.Data;

import java.util.Map;

@Data
public class AuthenticatorDTO {

    private String id;
    private String type;
    private String name;
    private String relatedAuthnId;
    private Boolean relatedWithMail;
    private Boolean relatedWithPhone;
    private String state;
    private Map<String, Object> config;
}
