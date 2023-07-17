package com.deepoove.authsaur.config;

import com.deepoove.authsaur.rest.controller.MfaApi;
import com.deepoove.authsaur.rest.controller.PwdlessApi;
import com.deepoove.authsaur.rest.controller.SamlApi;
import com.deepoove.authsaur.rest.controller.UserApi;
import com.deepoove.authsaur.rest.interceptor.LoginInterceptor;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration(proxyBeanMethods = false)
public class AuthsaurRestApiConfiguration {

    @Bean
    public LoginInterceptor loginInterceptor(CentralAuthenticationService centralAuthenticationService,
                                             CasCookieBuilder ticketGrantingTicketCookieGenerator) {
        return new LoginInterceptor(centralAuthenticationService, ticketGrantingTicketCookieGenerator);
    }

    @Bean
    public WebMvcConfigurer casWebAppWebMvcLoginConfigurer(
            LoginInterceptor loginInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(
                    final InterceptorRegistry registry) {
                registry.addInterceptor(loginInterceptor).addPathPatterns("/api/mfa/**", "/api/user/**");
            }
        };
    }

    @Bean
    public SamlApi samlServicesApi() {
        return new SamlApi();
    }

    @Bean
    public MfaApi mfaApi() {
        return new MfaApi();
    }

    @Bean
    public UserApi userApi() {
        return new UserApi();
    }

    @Bean
    public PwdlessApi passwordlessAuthenticationApi() {
        return new PwdlessApi();
    }


}
