package com.deepoove.authsaur.config;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import com.deepoove.authsaur.resolver.Base64SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration("SamlIdPBase64MetadataConfiguration")
public class SamlIdPBase64MetadataConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "base64SamlRegisteredServiceMetadataResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolver base64SamlRegisteredServiceMetadataResolver(final CasConfigurationProperties casProperties, @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME) final OpenSamlConfigBean openSamlConfigBean) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new Base64SamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "base64SamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer base64SamlRegisteredServiceMetadataResolutionPlanConfigurer(@Qualifier("base64SamlRegisteredServiceMetadataResolver") final SamlRegisteredServiceMetadataResolver base64SamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(base64SamlRegisteredServiceMetadataResolver);
    }
}
