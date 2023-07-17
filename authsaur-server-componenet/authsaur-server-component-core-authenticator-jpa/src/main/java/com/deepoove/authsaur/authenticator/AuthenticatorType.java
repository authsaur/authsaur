package com.deepoove.authsaur.authenticator;

import lombok.Getter;

@Getter
public enum AuthenticatorType {

    LDAP("ldap"),
    RADIUS("radius"),
    DINGTALK("dingtalk"),
    PASSWORD("pass_word"),
    WEIBO("weibo"),
    MFA_OTP("mfa_otp");

    private String type;

    AuthenticatorType(String type) {
        this.type = type;
    }
}
