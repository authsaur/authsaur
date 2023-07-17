package org.apereo.cas.config;

import com.deepoove.authsaur.setting.SettingsConf;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.AuthsaurTicketGrantingTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

// CasCoreTicketsBaseConfiguration
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurTicketsConfiguration {

    //    @ConditionalOnMissingBean(name = ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY)
    @Bean(name = ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ExpirationPolicyBuilder grantingTicketExpirationPolicy(
            final CasConfigurationProperties casProperties,
            final SettingsConf settingsConf) {
        return new AuthsaurTicketGrantingTicketExpirationPolicyBuilder(casProperties, settingsConf);
    }
}
