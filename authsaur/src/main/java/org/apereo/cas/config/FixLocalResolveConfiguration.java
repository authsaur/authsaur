package org.apereo.cas.config;


import com.deepoove.authsaur.rest.interceptor.FixLocaleChangeInterceptor;
import lombok.val;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.DefaultDelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;


@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class FixLocalResolveConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean("localeChangeInterceptor")
    public LocaleChangeInterceptor localeChangeInterceptor(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier(ArgumentExtractor.BEAN_NAME) final ArgumentExtractor argumentExtractor) {
        val interceptor = new FixLocaleChangeInterceptor(casProperties.getLocale(),
                argumentExtractor, servicesManager);
        interceptor.setParamName(casProperties.getLocale().getParamName());
        interceptor.setSupportedFlows(List.of(
                CasWebflowConfigurer.FLOW_ID_LOGOUT,
                CasWebflowConfigurer.FLOW_ID_LOGIN));
        return interceptor;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    public DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager(
            @Qualifier("delegatedClientAuthenticationConfigurationContext") final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
        return new DefaultDelegatedClientAuthenticationWebflowManager(delegatedClientAuthenticationConfigurationContext) {
            @Override
            protected Service restoreDelegatedAuthenticationRequest(RequestContext requestContext, WebContext webContext, TransientSessionTicket ticket) {
                val service = ticket.getService();

                WebUtils.putServiceIntoFlowScope(requestContext, service);
                webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);

                val themeParamName = delegatedClientAuthenticationConfigurationContext.getCasProperties().getTheme().getParamName();
                val localParamName = delegatedClientAuthenticationConfigurationContext.getCasProperties().getLocale().getParamName();

                val properties = ticket.getProperties();
                webContext.setRequestAttribute(themeParamName, properties.get(themeParamName));

//                val localeValue = properties.get(localParamName);
//                Optional.ofNullable(localeValue)
//                        .ifPresent(locale -> {
//                            webContext.setRequestAttribute(localParamName, locale);
//                            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
//                            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
//                            Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
//                                    .ifPresent(localeResolver -> localeResolver.setLocale(request, response, StringUtils.parseLocale(locale.toString())));
//                        });
                webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_METHOD, properties.get(CasProtocolConstants.PARAMETER_METHOD));
                return service;
            }
        };
    }


}
