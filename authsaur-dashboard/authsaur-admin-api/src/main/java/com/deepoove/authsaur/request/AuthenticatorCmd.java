package com.deepoove.authsaur.request;

import lombok.Data;

@Data
public class AuthenticatorCmd<T> {

    protected String id = "-1";
    protected String name;
    protected String state;

    protected String relatedAuthnId;
    protected boolean relatedWithMail = false;
    protected boolean relatedWithPhone = false;

    protected T properties;
}
