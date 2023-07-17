package com.deepoove.authsaur.config;


import com.deepoove.authsaur.advice.ExceptionAdvice;
import com.deepoove.authsaur.api.*;
import com.deepoove.authsaur.interceptor.AdminSecurityInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Slf4j
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurAdminApiConfiguration {


    @Bean
    public AdminSecurityInterceptor adminLoginInterceptor(RedisTemplate redisTemplate) {
        return new AdminSecurityInterceptor(redisTemplate);
    }

    @Bean
    public WebMvcConfigurer casAdminWebAppWebMvcLoginConfigurer(
            AdminSecurityInterceptor loginInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(
                    final InterceptorRegistry registry) {
                registry.addInterceptor(loginInterceptor).addPathPatterns("/api/audit/**",
                        "/api/authenticator/**",
                        "/api/organization/**",
                        "/api/service/**",
                        "/api/setting/**",
                        "/api/currentUser",
                        "/api/service_strategy/**",
                        "/api/usermanagement/**").excludePathPatterns("/api/login/**");
            }
        };
    }

    @Bean
    public ExceptionAdvice exceptionAdvice() {
        return new ExceptionAdvice();
    }

    @Bean
    public SettingApi settingsApi() {
        return new SettingApi();
    }

    @Bean
    public AuthenticatorApi authenticationHandlerApi() {
        return new AuthenticatorApi();
    }

    @Bean
    public UserManagementApi userManageApi() {
        return new UserManagementApi();
    }

    @Bean
    public OrganizationApi orgManagementApi() {
        return new OrganizationApi();
    }


    @Bean
    public ServiceApi servicesApi() {
        return new ServiceApi();
    }

    @Bean
    public ServiceStrategyApi strategyApi() {
        return new ServiceStrategyApi();
    }

    @Bean
    public AuditApi auditApi() {
        return new AuditApi();
    }

    @Bean
    public LoginOutApi loginApi() {
        return new LoginOutApi();
    }

}
