package com.deepoove.authsaur.config;

import com.deepoove.authsaur.setting.SettingScheduledLoader;
import com.deepoove.authsaur.setting.PrincipalNamedPolicy;
import com.deepoove.authsaur.setting.SettingsConf;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class JpaSettingsConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "settingsConf")
    public SettingsConf settingsConf(@Qualifier("dataSourceService") final DataSource dataSourceService) {
        return new SettingsConf(dataSourceService);
    }

    @Bean
    public PrincipalNamedPolicy principalNamedPolicy(SettingsConf settingService) {
        return new PrincipalNamedPolicy(settingService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "cas.custom.properties", name = "setting-enabled", havingValue = "true",
            matchIfMissing = true)
    public SettingScheduledLoader settingScheduledLoader(
            final SettingsConf settingsConf) {
        return new SettingScheduledLoader(settingsConf);
    }
}
