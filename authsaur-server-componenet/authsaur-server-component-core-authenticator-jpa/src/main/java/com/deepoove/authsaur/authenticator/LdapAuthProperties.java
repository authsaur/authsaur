package com.deepoove.authsaur.authenticator;

import lombok.Data;

import java.util.List;

@Data
public class LdapAuthProperties {

    private String baseDn;
    private String bindDn;
    private String bindCredential;
    private String ldapUrl;
    private boolean useStartTls;
    private String searchFilterTemplate;
    private String principalAttributeId;
    private List<String> principalAttributeList;

    private String emailAttribute;
    private String phoneAttribute;
    private String nameAttribute;

    private String searchFilter;
    private String emailSearchFilter;
    private String phoneSearchFilter;

}
