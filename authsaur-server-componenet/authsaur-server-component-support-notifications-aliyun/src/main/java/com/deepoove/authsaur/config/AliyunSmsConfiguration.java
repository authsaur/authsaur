package com.deepoove.authsaur.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.notifications.AliyunSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration("AliyunSmsConfiguration")
public class AliyunSmsConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public SmsSender smsSender() {
        return new AliyunSmsSender();
    }


}

