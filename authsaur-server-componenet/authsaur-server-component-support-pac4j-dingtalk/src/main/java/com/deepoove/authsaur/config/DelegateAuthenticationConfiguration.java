package com.deepoove.authsaur.config;

//import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.deepoove.authsaur.pac4j.CustomRefreshableDelegatedClients;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;
import com.deepoove.authsaur.web.DelegatedNavigationController;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;


@Configuration("DelegateAuthenticationConfiguration")
@Slf4j
public class DelegateAuthenticationConfiguration {

    @Autowired
    CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("pac4jDelegatedClientFactory")
    DelegatedClientFactory pac4jDelegatedClientFactory;

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean(name = "builtClients")
    public Clients builtClients() {
        return new CustomRefreshableDelegatedClients(casProperties.getServer().getLoginUrl(),
                pac4jDelegatedClientFactory);
    }


    @ConditionalOnMissingBean(name = "delegatedXXNavigationController")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedNavigationController delegatedXXNavigationController(@Qualifier(
            "delegatedClientAuthenticationConfigurationContext") final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext, @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME) final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager) {
        return new DelegatedNavigationController(delegatedClientAuthenticationConfigurationContext,
                delegatedClientWebflowManager);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer pac4jAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("builtClients") final Clients builtClients,
            @Qualifier("clientAuthenticationHandler") final AuthenticationHandler clientAuthenticationHandler,
            @Qualifier("clientAuthenticationMetaDataPopulator") final AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
                LOGGER.info("Registering delegated authentication clients...");
                plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler,
                        defaultPrincipalResolver);
                plan.registerAuthenticationMetadataPopulator(clientAuthenticationMetaDataPopulator);
        };
    }

}
