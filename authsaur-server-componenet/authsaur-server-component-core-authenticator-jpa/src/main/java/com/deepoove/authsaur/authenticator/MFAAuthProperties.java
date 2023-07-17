package com.deepoove.authsaur.authenticator;

import lombok.Data;

import java.util.List;

@Data
public class MFAAuthProperties {
    protected List<String> bypassUserIds;
    protected List<String> authIds;
    protected boolean trustedDeviceEnabled;


}
