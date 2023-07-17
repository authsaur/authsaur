package com.deepoove.authsaur.mfa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import com.deepoove.authsaur.authenticator.MFAAuthProperties;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationBypassAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AuthsaurMultifactorAuthenticationBypassAction extends MultifactorAuthenticationBypassAction {

    private final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;

    @Override
    protected Event doExecute(RequestContext requestContext) {

        // if bypass
        val bypass = provider.getBypassEvaluator();
        String id = provider.getId();

        JpaAuthenticationEntity authenticator = jpaAuthenticationHandlerRegistry.findById(id);
        if (null == authenticator) {
            throw new IllegalStateException("Cannot find mfa config");
        }
        MFAAuthProperties p = authenticator.readProperty(MFAAuthProperties.class);
        List<String> bypassUserIds = p.getBypassUserIds();

        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal());
        LOGGER.debug("Bypass rules determined MFA should or not execute for user [{}] for provider [{}]",
                principal.getId(), provider.getId());
        if (bypassUserIds.contains(principal.getId())) {
            bypass.rememberBypass(authentication, provider);
            LOGGER.debug("Authentication updated to remember bypass for user [{}] for provider [{}]",
                    principal.getId(), provider.getId());
            return yes();
        }

        return super.doExecute(requestContext);
    }
}
