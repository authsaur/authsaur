package com.deepoove.authsaur.authenticator;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class OTPAuthProperties extends MFAAuthProperties {
    private String issuer;
    private String label;

}
