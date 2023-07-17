package com.deepoove.authsaur.config;

import com.deepoove.authsaur.audit.AuthsaurCredentialsAsFirstParameterResourceResolver;
import com.deepoove.authsaur.audit.AuthsaurThreadLocalAuditPrincipalResolver;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurCoreAuditConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditResourceResolver credentialsAsFirstParameterResourceResolver(@Qualifier(
            "handledAuthenticationExceptions") final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
        return new AuthsaurCredentialsAsFirstParameterResourceResolver(handledAuthenticationExceptions);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalResolver auditablePrincipalResolver(
            @Qualifier("auditPrincipalIdProvider") final AuditPrincipalIdProvider auditPrincipalIdProvider) {
        return new AuthsaurThreadLocalAuditPrincipalResolver(auditPrincipalIdProvider);
    }


}
