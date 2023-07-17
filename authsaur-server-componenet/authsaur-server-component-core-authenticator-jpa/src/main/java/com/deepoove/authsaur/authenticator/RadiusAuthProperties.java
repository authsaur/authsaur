package com.deepoove.authsaur.authenticator;

import lombok.Data;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusServerProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@Data
public class RadiusAuthProperties {

    private boolean failoverOnException;
    private boolean failoverOnAuthenticationFailure;

    // server
    private String protocol = "EAP_MSCHAPv2";
    private int retries = 3;

    // client
    private String inetAddress = "localhost";
    private String sharedSecret = "N0Sh@ar3d$ecReT";
    private int socketTimeout;
    private int authenticationPort = 1812;
    private int accountingPort = 1813;

    // password
//    private String passwordEncoderType = "NONE";
//    private String encodingAlgorithm;
//    private String characterEncoding = "UTF-8";
//    private String secret;
//    private int strength = 16;

}
