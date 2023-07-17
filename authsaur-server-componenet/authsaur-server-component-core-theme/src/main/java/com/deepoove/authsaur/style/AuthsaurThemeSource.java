package com.deepoove.authsaur.style;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@Slf4j
public class AuthsaurThemeSource extends ResourceBundleThemeSource {
    private final CasConfigurationProperties casProperties;
    private final MessageSource parent;

    @Override
    protected MessageSource createMessageSource(
            @Nonnull final String basename) {
        // TOOD parent new here and set basename for every message source theme
        MessageSource messageSource = super.createMessageSource(basename);
        ((HierarchicalMessageSource) messageSource).setParentMessageSource(parent);
        return messageSource;
    }
}
