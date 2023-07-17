package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.delegated.AutoRedirectDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurDelegatedRedirectConfiguration {

    @Bean
    public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy(
            final CasConfigurationProperties casProperties
    ) {
        return new AutoRedirectDelegatedClientIdentityProviderRedirectionStrategy(casProperties);
    }


}

