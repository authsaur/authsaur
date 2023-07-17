package com.deepoove.authsaur.mfa;

import lombok.extern.slf4j.Slf4j;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.DefaultCasDelegatingWebflowEventResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AuthsaurMFADelegatingWebflowEventResolver extends DefaultCasDelegatingWebflowEventResolver {

    final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;

    public AuthsaurMFADelegatingWebflowEventResolver(CasWebflowEventResolutionConfigurationContext configurationContext
            , CasWebflowEventResolver selectiveResolver,
                                                     JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry) {
        super(configurationContext, selectiveResolver);
        this.jpaAuthenticationHandlerRegistry = jpaAuthenticationHandlerRegistry;
    }

    protected Collection<Event> resolveCandidateAuthenticationEvents(final RequestContext context,
                                                                     final Service service,
                                                                     final RegisteredService registeredService) {
        List<JpaAuthenticationEntity> query = jpaAuthenticationHandlerRegistry.queryActiveMFA();
        if (query.size() == 0) {
            LOGGER.info("Not config mfa authenticator and not trigger mfa.");
            return Collections.emptyList();
        }
        return super.resolveCandidateAuthenticationEvents(context, service, registeredService);
    }
}
