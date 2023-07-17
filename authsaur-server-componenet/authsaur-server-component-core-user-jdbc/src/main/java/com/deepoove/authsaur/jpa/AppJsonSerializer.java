package com.deepoove.authsaur.jpa;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.PrettyPrinter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Serializes registered services to JSON based on the Jackson JSON library.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@NoArgsConstructor
public class AppJsonSerializer extends AbstractJacksonBackedStringSerializer<RegisteredServiceAccessStrategy> {

    private static final long serialVersionUID = 7645698151115635245L;

    public AppJsonSerializer(final PrettyPrinter prettyPrinter) {
        super(prettyPrinter);
    }

    @Override
    public boolean supports(final File file) {
        try {
            val content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
            return supports(content);
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean supports(final String content) {
        return content.contains(JsonTypeInfo.Id.CLASS.getDefaultPropertyName());
    }

    @Override
    public Class<RegisteredServiceAccessStrategy> getTypeToSerialize() {
        return RegisteredServiceAccessStrategy.class;
    }

    @Override
    public List<MediaType> getContentTypes() {
        return List.of(MediaType.APPLICATION_JSON);
    }
}
