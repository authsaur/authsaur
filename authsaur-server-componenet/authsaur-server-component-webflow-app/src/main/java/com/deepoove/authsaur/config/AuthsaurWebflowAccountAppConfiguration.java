package com.deepoove.authsaur.config;

import com.deepoove.authsaur.configurer.AccountAppWebflowConfigurer;
import com.deepoove.authsaur.configurer.action.FetchTGTAction;
import com.deepoove.authsaur.configurer.action.PrepareAccountAppViewAction;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.jpa.JpaOrgRegistry;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;


@Configuration(value = "CasWebflowAccountAppConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurWebflowAccountAppConfiguration {
    private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

    @ConditionalOnMissingBean(name = "accountAppWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer accountAppWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier("accountAppFlowRegistry") final FlowDefinitionRegistry accountAppFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
        return new AccountAppWebflowConfigurer(flowBuilderServices,
                accountAppFlowRegistry, loginFlowRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "accountAppWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer accountAppWebflowExecutionPlanConfigurer(
            @Qualifier("accountAppWebflowConfigurer") final CasWebflowConfigurer accountAppWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(accountAppWebflowConfigurer);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public FlowExecutor accountAppFlowExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier("accountAppFlowRegistry") final FlowDefinitionRegistry accountAppFlowRegistry,
            @Qualifier("webflowCipherExecutor") final CipherExecutor webflowCipherExecutor) {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
                accountAppFlowRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS);
        return factory.build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public FlowDefinitionRegistry accountAppFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER) final FlowBuilder flowBuilder) {
        val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, AccountAppWebflowConfigurer.FLOW_ID);
        return builder.build();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public HandlerAdapter accountAppWebflowHandlerAdapter(
            @Qualifier("accountAppFlowExecutor") final FlowExecutor accountAppFlowExecutor) {
        val handler = new CasFlowHandlerAdapter(AccountAppWebflowConfigurer.FLOW_ID);
        handler.setFlowExecutor(accountAppFlowExecutor);
        handler.setFlowUrlHandler(new CasDefaultFlowUrlHandler());
        return handler;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HandlerMapping accountAppFlowHandlerMapping(
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME) final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier("accountAppFlowRegistry") final FlowDefinitionRegistry accountAppFlowRegistry) {
        val handler = new FlowHandlerMapping();
        handler.setOrder(0);
        handler.setFlowRegistry(accountAppFlowRegistry);
        handler.setInterceptors(webflowExecutionPlan.getWebflowInterceptors().toArray());
        return handler;
    }

    @Bean
    @ConditionalOnMissingBean(name = "prepareAccountAppViewAction")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action prepareAccountAppViewAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcAuditTrailManager") final AuditTrailManager auditTrailManager,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("centralAuthenticationService") final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("googleAuthenticatorAccountRegistry") final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry,
            JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry,
            JpaOrgRegistry jpaOrgRegistry) {
        return new PrepareAccountAppViewAction(centralAuthenticationService,
                servicesManager, casProperties, auditTrailManager, googleAuthenticatorAccountRegistry,
                jpaAuthenticationHandlerRegistry, jpaOrgRegistry);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "fetchTGTAction")
    public Action fetchTGTAction(
            @Qualifier("ticketGrantingTicketCookieGenerator") final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier("centralAuthenticationService") final CentralAuthenticationService centralAuthenticationService) {
        return new FetchTGTAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator);
    }


}
