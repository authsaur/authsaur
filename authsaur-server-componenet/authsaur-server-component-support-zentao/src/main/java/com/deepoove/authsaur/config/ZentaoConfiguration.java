package com.deepoove.authsaur.config;

import com.deepoove.authsaur.services.ZentaoRegisteredService;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import com.deepoove.authsaur.zentao.ZentaoConfigurationContext;
import com.deepoove.authsaur.zentao.ZentaoServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import com.deepoove.authsaur.zentao.ZentaoEndpointController;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;


@Configuration("ZentaoConfiguration")
//@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ZentaoConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Bean(name = "zentaoCallbackService")
    public Service zentaoCallbackService() {
        return webApplicationServiceFactory.createService(casProperties.getServer().getPrefix()
                + "/zentao/Callback");
    }

    @Bean
    @ConditionalOnMissingBean(name = "zentaolessServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer zentaolessServiceRegistryExecutionPlanConfigurer(@Qualifier(
            "zentaoCallbackService") final Service zentaoCallbackService) {
        return plan -> {
            String callbackService = zentaoCallbackService.getId().concat(".*");
            RegexRegisteredService service = new RegexRegisteredService();
            service.setId(RandomUtils.nextLong());
            service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("Pwd Authentication Callback Request URL");
            service.setServiceId(callbackService);
//            service.setLogoutType(RegisteredServiceLogoutType.FRONT_CHANNEL);
//            service.setLogoutUrl("http://192.168.2.79:8000/user-logout.html");
//            service.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
            plan.registerServiceRegistry(new ZentaoServiceRegistry(applicationContext, service));
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "zentaoTicketValidator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketValidator zentaoTicketValidator(
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier(CentralAuthenticationService.BEAN_NAME) final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("authenticationAttributeReleasePolicy") final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
        return new InternalTicketValidator(centralAuthenticationService, webApplicationServiceFactory,
                authenticationAttributeReleasePolicy, servicesManager);
    }

    @Bean
    @RefreshScope
    public ZentaoConfigurationContext zentaoConfigurationContext(@Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
                                                                 @Qualifier("zentaoCallbackService") final Service zentaoCallbackService,
                                                                 @Qualifier("zentaoTicketValidator") final TicketValidator zentaoTicketValidator,
                                                                 @Qualifier(TicketRegistrySupport.BEAN_NAME) final TicketRegistrySupport ticketRegistrySupport,
                                                                 @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER) final CasCookieBuilder ticketGrantingTicketCookieGenerator) {
        ZentaoConfigurationContext context = new ZentaoConfigurationContext();
        context.setServicesManager(servicesManager);
        context.setTicketValidator(zentaoTicketValidator);
        context.setCallbackService(zentaoCallbackService);
        context.setTicketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator);
        context.setTicketRegistrySupport(ticketRegistrySupport);
        context.setWebApplicationServiceFactory(webApplicationServiceFactory);
        context.setCasProperties(casProperties);
        return context;

    }

    @Bean(name = "zentaoComponentSerializationConfiguration")
    public ComponentSerializationPlanConfigurer zentaoComponentSerializationConfiguration() {
        return plan -> plan.registerSerializableClass(ZentaoRegisteredService.class);
    }

    @Bean("PasswordlessEndpointController")
    public ZentaoEndpointController PasswordlessEndpointController() {
        return new ZentaoEndpointController();
    }
}
