package com.deepoove.authsaur.config;

import com.deepoove.authsaur.configurer.UserWebflowConfigurer;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;
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
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;


@Configuration(value = "CasWebflowUserConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurWebflowUserConfiguration {
    private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

    @ConditionalOnMissingBean(name = "userWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer userWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier("userFlowRegistry") final FlowDefinitionRegistry userFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
        return new UserWebflowConfigurer(flowBuilderServices,
                userFlowRegistry, loginFlowRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "userWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer userWebflowExecutionPlanConfigurer(
            @Qualifier("userWebflowConfigurer") final CasWebflowConfigurer userWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(userWebflowConfigurer);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public FlowExecutor userFlowExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier("userFlowRegistry") final FlowDefinitionRegistry userFlowRegistry,
            @Qualifier("webflowCipherExecutor") final CipherExecutor webflowCipherExecutor) {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
                userFlowRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS);
        return factory.build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public FlowDefinitionRegistry userFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER) final FlowBuilder flowBuilder) {
        val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, UserWebflowConfigurer.FLOW_ID);
        return builder.build();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public HandlerAdapter userWebflowHandlerAdapter(
            @Qualifier("userFlowExecutor") final FlowExecutor userFlowExecutor) {
        val handler = new CasFlowHandlerAdapter(UserWebflowConfigurer.FLOW_ID);
        handler.setFlowExecutor(userFlowExecutor);
        handler.setFlowUrlHandler(new CasDefaultFlowUrlHandler());
        return handler;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HandlerMapping userFlowHandlerMapping(
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME) final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier("userFlowRegistry") final FlowDefinitionRegistry userFlowRegistry) {
        val handler = new FlowHandlerMapping();
        handler.setOrder(0);
        handler.setFlowRegistry(userFlowRegistry);
        handler.setInterceptors(webflowExecutionPlan.getWebflowInterceptors().toArray());
        return handler;
    }


}
