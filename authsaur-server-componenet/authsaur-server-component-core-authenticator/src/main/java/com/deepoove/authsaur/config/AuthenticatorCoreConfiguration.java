package com.deepoove.authsaur.config;

import com.deepoove.authsaur.authentication.AuthenticatorManager;
import com.deepoove.authsaur.authentication.AuthenticatorScheduledLoader;
import com.deepoove.authsaur.authentication.DefaultAuthenticatorManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthenticatorCoreConfiguration {

    @Bean
    public DefaultAuthenticatorManager authenticatorManager() {
        return new DefaultAuthenticatorManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cas.custom.properties", name = "authn-enabled", havingValue = "true",
            matchIfMissing = true)
    public AuthenticatorScheduledLoader authenticatorManagerScheduledLoader(
            final AuthenticatorManager authenticatorManager) {
        return new AuthenticatorScheduledLoader(authenticatorManager);
    }


}
