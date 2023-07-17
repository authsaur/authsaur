package com.deepoove.authsaur.zentao;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.jasig.cas.client.validation.TicketValidator;


public class ZentaoConfigurationContext {
    private ServicesManager servicesManager;
    private TicketValidator ticketValidator;
    private Service callbackService;
    private CasConfigurationProperties casProperties;

    private CasCookieBuilder ticketGrantingTicketCookieGenerator;
    private TicketRegistrySupport ticketRegistrySupport;
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public TicketValidator getTicketValidator() {
        return ticketValidator;
    }

    public void setTicketValidator(TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    public Service getCallbackService() {
        return callbackService;
    }

    public void setCallbackService(Service callbackService) {
        this.callbackService = callbackService;
    }

    public CasConfigurationProperties getCasProperties() {
        return casProperties;
    }

    public void setCasProperties(CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    public CasCookieBuilder getTicketGrantingTicketCookieGenerator() {
        return ticketGrantingTicketCookieGenerator;
    }

    public void setTicketGrantingTicketCookieGenerator(CasCookieBuilder ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public TicketRegistrySupport getTicketRegistrySupport() {
        return ticketRegistrySupport;
    }

    public void setTicketRegistrySupport(TicketRegistrySupport ticketRegistrySupport) {
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    public ServiceFactory<WebApplicationService> getWebApplicationServiceFactory() {
        return webApplicationServiceFactory;
    }

    public void setWebApplicationServiceFactory(ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }
}
