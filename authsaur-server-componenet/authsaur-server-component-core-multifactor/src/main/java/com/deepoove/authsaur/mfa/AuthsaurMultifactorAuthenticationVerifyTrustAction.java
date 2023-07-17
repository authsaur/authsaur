package com.deepoove.authsaur.mfa;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.audit.AuditableExecution;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import com.deepoove.authsaur.authenticator.MFAAuthProperties;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationVerifyTrustAction;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
public class AuthsaurMultifactorAuthenticationVerifyTrustAction extends MultifactorAuthenticationVerifyTrustAction {
    private final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;

    public AuthsaurMultifactorAuthenticationVerifyTrustAction(MultifactorAuthenticationTrustStorage storage,
                                                              DeviceFingerprintStrategy deviceFingerprintStrategy,
                                                              TrustedDevicesMultifactorProperties trustedProperties,
                                                              AuditableExecution registeredServiceAccessStrategyEnforcer, MultifactorAuthenticationTrustedDeviceBypassEvaluator bypassEvaluator, JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry) {
        super(storage, deviceFingerprintStrategy, trustedProperties, registeredServiceAccessStrategyEnforcer,
                bypassEvaluator);
        this.jpaAuthenticationHandlerRegistry = jpaAuthenticationHandlerRegistry;
    }

    @Override
    protected Event doExecute(RequestContext requestContext) {
        val authn = WebUtils.getAuthentication(requestContext);
        if (authn == null) {
            LOGGER.warn("Could not determine authentication from the request context");
            return no();
        }
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val service = WebUtils.getService(requestContext);
        // is enable trusted device
        val id = WebUtils.getMultifactorAuthenticationProviderById(requestContext);
        JpaAuthenticationEntity authenticator = jpaAuthenticationHandlerRegistry.findById(id);
        if (null == authenticator) {
            throw new IllegalStateException("cannot find mfa config");
        }
        MFAAuthProperties p = authenticator.readProperty(MFAAuthProperties.class);
        if (!p.isTrustedDeviceEnabled()) {
            LOGGER.debug("Trusted device registration is disabled for [{}]", registeredService);
            return result(CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        return super.doExecute(requestContext);
    }
}
