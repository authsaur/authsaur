package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthsaurOAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.authentication.AuthsaurServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurOverlayConfiguration {

    @Bean
//    @ConditionalOnMissingBean(name = "serviceMatchingStrategy")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceMatchingStrategy serviceMatchingStrategy(
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
        return new AuthsaurServiceMatchingStrategy(servicesManager);
    }

    //    @ConditionalOnMissingBean(name = "oauthUserProfileViewRenderer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer(final CasConfigurationProperties casProperties) {
        return new AuthsaurOAuth20DefaultUserProfileViewRenderer(casProperties.getAuthn().getOauth());
    }
}
