package org.apereo.cas.config;

import lombok.val;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configurer.AuthsaurLoginWebflowConfigurer;
import org.apereo.cas.configurer.AuthsaurLogoutWebflowConfigurer;
import org.apereo.cas.configurer.action.PrepareLoginSetupAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;


@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CasWebflowContextConfiguration.CasWebflowContextExecutionPlanConfiguration.class)
public class AuthsaurLoginOutWebflowConfiguration {

    //    @ConditionalOnMissingBean(name = "defaultWebflowConfigurer")
    @Bean("defaultWebflowConfigurer")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Lazy(false)
    public CasWebflowConfigurer defaultWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("loginFlowRegistry") final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier("logoutFlowRegistry") final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier("flowBuilderServices") final FlowBuilderServices flowBuilderServices) {
        val c = new AuthsaurLoginWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext,
                casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    //    @ConditionalOnMissingBean(name = "defaultLogoutWebflowConfigurer")
    @Bean("defaultLogoutWebflowConfigurer")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Lazy(false)
    public CasWebflowConfigurer defaultLogoutWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("loginFlowRegistry") final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier("logoutFlowRegistry") final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier("flowBuilderServices") final FlowBuilderServices flowBuilderServices) {
        val c = new AuthsaurLogoutWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext,
                casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "prepareLoginSetupAction")
    public Action prepareLoginSetupAction(
            @Qualifier("authenticationEventExecutionPlan") final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
            final CasConfigurationProperties casProperties,
            @Qualifier("builtClients") Clients clients) {
        return new PrepareLoginSetupAction(authenticationEventExecutionPlan, casProperties, clients);
    }

}
