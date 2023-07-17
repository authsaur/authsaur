package com.deepoove.authsaur.config;

import com.deepoove.authsaur.style.AuthsaurMessageBundle;
import com.deepoove.authsaur.style.AuthsaurThemeSource;
import com.deepoove.authsaur.style.CssResource;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.setting.SettingsConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.ui.context.ThemeSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;

@Configuration(proxyBeanMethods = false)
public class AuthsaurStyleConfiguration {

    @Autowired
    private SettingsConf settingsConf;

    //@ConditionalOnMissingBean(name = "casThemeSource")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ThemeSource themeSource(final CasConfigurationProperties casProperties,
                                   @Qualifier("messageSource") final MessageSource messageSource) {
        MessageSource parent = new AuthsaurMessageBundle(settingsConf);
        if (messageSource instanceof HierarchicalMessageSource) {
            // i18 message source
            ((HierarchicalMessageSource) messageSource).setParentMessageSource(parent);
            // theme message source
            return new AuthsaurThemeSource(casProperties, parent);
        }
        return new AuthsaurThemeSource(casProperties, parent);
    }

    @Bean
    public WebMvcConfigurer styleWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                CssResource cssByteArrayResource = new CssResource(settingsConf,
                        "".getBytes(StandardCharsets.UTF_8));
                registry.addResourceHandler("/stylecss/custom.css").addResourceLocations(cssByteArrayResource)
                        .setCachePeriod(0);
            }
        };
    }


}
