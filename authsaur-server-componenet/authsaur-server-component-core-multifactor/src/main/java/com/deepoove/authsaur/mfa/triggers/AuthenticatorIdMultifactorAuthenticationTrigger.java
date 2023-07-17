package com.deepoove.authsaur.mfa.triggers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import com.deepoove.authsaur.authenticator.AuthenticatorType;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import com.deepoove.authsaur.authenticator.MFAAuthProperties;
import org.apereo.cas.authentication.principal.Service;
import com.deepoove.authsaur.principal.MinePrincipal;
import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
public class AuthenticatorIdMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {

    private final ConfigurableApplicationContext applicationContext;
    private final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(Authentication authentication,
                                                                   RegisteredService registeredService,
                                                                   HttpServletRequest httpServletRequest,
                                                                   HttpServletResponse httpServletResponse,
                                                                   Service service) {
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val providerMap =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return Optional.empty();
        }

        List<JpaAuthenticationEntity> mfas = jpaAuthenticationHandlerRegistry.queryActiveMFA();
        for (JpaAuthenticationEntity mfa : mfas) {
            MFAAuthProperties mfaAuthProperties = mfa.readProperty(MFAAuthProperties.class);
            List<String> controlAuthIds = mfaAuthProperties.getAuthIds();
            MinePrincipal minePrincipal = new MinePrincipal(authentication.getPrincipal());
            String authId = null == minePrincipal.getRelatedPrincipal() ? minePrincipal.getAuthId() :
                    minePrincipal.getRelatedPrincipal().getAuthId();
            String authType = null == minePrincipal.getRelatedPrincipal() ? minePrincipal.getAuthType() :
                    minePrincipal.getRelatedPrincipal().getAuthType();

            if (null != controlAuthIds && null != authId) {
                if (controlAuthIds.contains(authId)) {
                    //  open in dingtalk
                    if (AuthenticatorType.DINGTALK.getType().equals(authType)) {
                        Map<String, List<Object>> authenticationAttributes = authentication.getAttributes();
                        List<Object> userAgent = authenticationAttributes.get("userAgent");
                        String agent = userAgent.get(0) != null ? userAgent.get(0).toString() : "";
                        if (agent.toLowerCase().contains("dingtalk")) {
                            return Optional.empty();
                        }
                    }
                    return providerMap.values().stream().filter(e -> e.getId().equals(mfa.getId())).findFirst();
                }
            }

        }
        return Optional.empty();

    }

    @Override
    public boolean supports(HttpServletRequest request, RegisteredService registeredService,
                            Authentication authentication, Service service) {
        return MultifactorAuthenticationTrigger.super.supports(request, registeredService, authentication, service);
    }
}

