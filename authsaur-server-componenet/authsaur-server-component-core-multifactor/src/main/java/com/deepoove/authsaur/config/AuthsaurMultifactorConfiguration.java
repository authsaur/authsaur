package com.deepoove.authsaur.config;

import com.deepoove.authsaur.mfa.AuthsaurMFADelegatingWebflowEventResolver;
import com.deepoove.authsaur.mfa.AuthsaurMultifactorAuthenticationBypassAction;
import com.deepoove.authsaur.mfa.AuthsaurMultifactorAuthenticationVerifyTrustAction;
import com.deepoove.authsaur.mfa.AuthsaurOneTimeTokenAccountCreateRegistrationAction;
import com.deepoove.authsaur.mfa.triggers.AuthenticatorIdMultifactorAuthenticationTrigger;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

@Configuration(proxyBeanMethods = false)
public class AuthsaurMultifactorConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationTrigger authenticatorIdMultifactorAuthenticationTrigger(
            final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry,
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
        return new AuthenticatorIdMultifactorAuthenticationTrigger(
                applicationContext,
                jpaAuthenticationHandlerRegistry);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowEventResolver authenticatorIdAuthenticationPolicyWebflowEventResolver(
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
            CasDelegatingWebflowEventResolver initialEventResolver,
            final CasConfigurationProperties casProperties,
            @Qualifier("authenticatorIdMultifactorAuthenticationTrigger") final MultifactorAuthenticationTrigger authenticatorIdMultifactorAuthenticationTrigger,
            @Qualifier("casWebflowConfigurationContext") final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        var r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
                casWebflowConfigurationContext, authenticatorIdMultifactorAuthenticationTrigger);
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getCore().getIssuer())) {
            initialEventResolver.addDelegate(r);
        }
        return r;
    }


    @Bean(name = "initialAuthenticationAttemptWebflowEventResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver(
            final CasConfigurationProperties casProperties,
            final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry,
            @Qualifier("selectiveAuthenticationProviderWebflowEventResolver") final CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver,
            @Qualifier("casWebflowConfigurationContext") final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
            @Qualifier("adaptiveAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver,
            @Qualifier("timedAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver,
            @Qualifier("globalAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver,
            @Qualifier("httpRequestAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver,
            @Qualifier("restEndpointAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver,
            @Qualifier("groovyScriptAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver,
            @Qualifier("scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver,
            @Qualifier("registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver,
            @Qualifier("predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver") final CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver,
            @Qualifier("principalAttributeAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver,
            @Qualifier("authenticationAttributeAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver,
            @Qualifier("registeredServiceAuthenticationPolicyWebflowEventResolver") final CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver) {

        val resolver = new AuthsaurMFADelegatingWebflowEventResolver(casWebflowConfigurationContext,
                selectiveAuthenticationProviderWebflowEventResolver,
                jpaAuthenticationHandlerRegistry);
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getCore().getIssuer())) {
            resolver.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(timedAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(globalAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(httpRequestAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(groovyScriptAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver);
            resolver.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(authenticationAttributeAuthenticationPolicyWebflowEventResolver);
            resolver.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver);
        }
        return resolver;
    }


    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action googleAccountCreateRegistrationAction(
            final CasConfigurationProperties casProperties,
            final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry,
            @Qualifier("googleAuthenticatorAccountRegistry") final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
        val gauth = casProperties.getAuthn().getMfa().getGauth().getCore();
        return new AuthsaurOneTimeTokenAccountCreateRegistrationAction(googleAuthenticatorAccountRegistry,
                jpaAuthenticationHandlerRegistry);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action mfaBypassAction(final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry) {
        return new AuthsaurMultifactorAuthenticationBypassAction(jpaAuthenticationHandlerRegistry);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action mfaVerifyTrustAction(final CasConfigurationProperties casProperties,
                                       final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry,
                                       @Qualifier("mfaTrustedDeviceBypassEvaluator") final MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator,
                                       @Qualifier("deviceFingerprintStrategy") final DeviceFingerprintStrategy deviceFingerprintStrategy,
                                       @Qualifier("mfaTrustEngine") final MultifactorAuthenticationTrustStorage mfaTrustEngine,
                                       @Qualifier("registeredServiceAccessStrategyEnforcer") final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new AuthsaurMultifactorAuthenticationVerifyTrustAction(mfaTrustEngine,
                deviceFingerprintStrategy,
                casProperties.getAuthn().getMfa().getTrusted(),
                registeredServiceAccessStrategyEnforcer,
                mfaTrustedDeviceBypassEvaluator,
                jpaAuthenticationHandlerRegistry);
    }
}
