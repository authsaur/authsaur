package com.deepoove.authsaur.configurer.action;


import lombok.Data;

import java.util.Map;

@Data
public class ServiceVO {

    private long id;
    private String serviceId;
    private String logoutUrl;
    private String type;
    private String name;
    private String logo;
    private String saml;
    private String homePage;
    private String tag;
    private String description;

}

