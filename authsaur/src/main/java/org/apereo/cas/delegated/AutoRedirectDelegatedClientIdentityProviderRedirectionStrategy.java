package org.apereo.cas.delegated;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.PropConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

@RequiredArgsConstructor
public class AutoRedirectDelegatedClientIdentityProviderRedirectionStrategy implements DelegatedClientIdentityProviderRedirectionStrategy {

    private final CasConfigurationProperties casProperties;

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> getPrimaryDelegatedAuthenticationProvider(RequestContext context, WebApplicationService service, DelegatedClientIdentityProviderConfiguration provider) {
        String primary = casProperties.getCustom().getProperties().get(PropConstants.primaryIdp);
        if (StringUtils.isNotBlank(primary) && provider.getName().equalsIgnoreCase(primary)) {
            provider.setAutoRedirectType(DelegationAutoRedirectTypes.SERVER);
            return Optional.of(provider);
        }
        return Optional.empty();
    }
}
