package com.deepoove.authsaur.model.service;

import lombok.Data;

@Data
public class GrafanaConfig {

    protected String url;
    protected String clientId;
    protected String clientSecret;
}
