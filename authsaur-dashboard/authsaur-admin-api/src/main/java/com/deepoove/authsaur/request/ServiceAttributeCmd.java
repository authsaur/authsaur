package com.deepoove.authsaur.request;

import lombok.Data;

@Data
public class ServiceAttributeCmd {
    private long id;
    private String principalAttr;
    private String umail;
    private String uname;
    private String uphone;
}
