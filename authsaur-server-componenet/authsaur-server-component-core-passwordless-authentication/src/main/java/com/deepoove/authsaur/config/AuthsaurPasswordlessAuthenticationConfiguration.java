package com.deepoove.authsaur.config;

import com.deepoove.authsaur.configurer.AcceptPasswordlessAuthenticationAction;
import com.deepoove.authsaur.configurer.AuthsaurPasswordlessWebflowConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;


@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurPasswordlessAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "authsaurPasswordlessWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer authsaurPasswordlessWebflowConfigurer(final CasConfigurationProperties casProperties,
                                                                      final ConfigurableApplicationContext applicationContext, @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry, @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
        return new AuthsaurPasswordlessWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authsaurPasswordlessWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer authsaurPasswordlessWebflowExecutionPlanConfigurer(@Qualifier(
            "authsaurPasswordlessWebflowConfigurer") final CasWebflowConfigurer authsaurPasswordlessWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(authsaurPasswordlessWebflowConfigurer);
    }


    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action acceptPasswordlessAuthenticationAction(@Qualifier("passwordlessUserAccountStore") final PasswordlessUserAccountStore passwordlessUserAccountStore, @Qualifier("passwordlessTokenRepository") final PasswordlessTokenRepository passwordlessTokenRepository, @Qualifier("adaptiveAuthenticationPolicy") final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy, @Qualifier(AuthenticationSystemSupport.BEAN_NAME) final AuthenticationSystemSupport authenticationSystemSupport, @Qualifier("serviceTicketRequestWebflowEventResolver") final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver, @Qualifier("initialAuthenticationAttemptWebflowEventResolver") final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        return new AcceptPasswordlessAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, passwordlessTokenRepository,
                authenticationSystemSupport, passwordlessUserAccountStore);
    }

    @Bean("passwordlessAuthenticationEventExecutionPlanConfigurer")
    public AuthenticationEventExecutionPlanConfigurer passwordlessAuthenticationEventExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordlessTokenAuthenticationHandler") final AuthenticationHandler passwordlessTokenAuthenticationHandler,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getPasswordless().getTokens().getMail().getText()) ||
                    StringUtils.isNotBlank(casProperties.getAuthn().getPasswordless().getTokens().getSms().getText())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(passwordlessTokenAuthenticationHandler,
                        defaultPrincipalResolver);
            }
        };
    }
}
