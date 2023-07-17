package com.deepoove.authsaur.resolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.codec.binary.Base64;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Slf4j
public class Base64SamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    public Base64SamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                       final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service,
                                                          final CriteriaSet criteriaSet) {
        String location = service.getMetadataLocation();
        String content = new String(Base64.decodeBase64(location.substring("base64://".length())),
                StandardCharsets.UTF_8);
        LOGGER.info(content);
        SamlMetadataDocument doc = SamlMetadataDocument.builder().id(System.nanoTime()).name(service.getName()).value(
                content).build();
        val resolver = buildMetadataResolverFrom(service, doc);
        return CollectionUtils.wrapList(resolver);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("base64://");
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }
}
