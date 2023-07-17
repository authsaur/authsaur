package com.deepoove.authsaur.authentication;

import com.deepoove.authsaur.authenticator.AuthenticatorType;
import com.deepoove.authsaur.authenticator.DingtalkAuthProperties;
import com.deepoove.authsaur.authenticator.OAuthAuthProperties;
import com.deepoove.authsaur.authenticator.RadiusAuthProperties;
import com.deepoove.authsaur.jpa.*;
import com.deepoove.authsaur.pac4j.CustomRefreshableDelegatedClients;
import com.deepoove.authsaur.pac4j.dingtalk.DingTalkClient;
import com.deepoove.authsaur.pac4j.dingtalkless.DingTalklessClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.core.http.callback.PathParameterCallbackUrlResolver;
import org.pac4j.oauth.client.WeiboClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class DefaultAuthenticatorManager implements AuthenticatorManager, InitializingBean {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;
    @Autowired
    private JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Autowired
    @Qualifier("ldapPrincipalFactory")
    private PrincipalFactory ldapPrincipalFactory;
    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    private PrincipalResolver defaultPrincipalResolver;
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    @Qualifier(CasSSLContext.BEAN_NAME)
    private CasSSLContext casSslContext;
    @Qualifier("builtClients")
    @Autowired
    private Clients clients;

    private void activeAuthn(JpaAuthenticationEntity handler) {
        if ("-1024".equals(handler.getId())) {
            doAuthsaurPassword(handler);
        }
        if (!handler.getState().equals(AuthenticationHandlerStates.ACTIVE.toString())) return;
        if (handler.getType().equals(AuthenticatorType.LDAP.getType())) {
            activeLdapHandler(handler);
        } else if (handler.getType().equals(AuthenticatorType.DINGTALK.getType())) {
            activeDingtalk(handler);
        } else if (handler.getType().equals(AuthenticatorType.RADIUS.getType())) {
            activeRadiusHandler(handler);
        } else if (handler.getType().equals(AuthenticatorType.WEIBO)) {
            activeWeibo(handler);
        }
    }

    private void doAuthsaurPassword(JpaAuthenticationEntity entity) {
        try {
            final Map<AuthenticationHandler, PrincipalResolver> map = reflectAuthenticationHandlerMap();
            map.keySet().forEach(e -> {
                if (e instanceof AbstractAuthenticationHandler && e.getName().equals("-1024")) {
                    if (entity.getState().equals(AuthenticationHandlerStates.ACTIVE.toString())) {
                        ((AbstractAuthenticationHandler) e).setState(AuthenticationHandlerStates.ACTIVE);
                    } else {
                        ((AbstractAuthenticationHandler) e).setState(AuthenticationHandlerStates.STANDBY);
                    }
                }
            });

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


    }

    private void activeLdapHandler(JpaAuthenticationEntity entity) {
        LdapAuthenticationProperties l = entity.readProperty(LdapAuthenticationProperties.class);
        l.setName(String.valueOf(entity.getId()));
        LdapAuthenticationHandler handler = LdapUtils.createLdapAuthenticationHandler(l, applicationContext,
                servicesManager, ldapPrincipalFactory);
        handler.setState(l.getState());
        authenticationEventExecutionPlan.registerAuthenticationHandlerWithPrincipalResolver(handler,
                defaultPrincipalResolver);
    }

    private void activeRadiusHandler(JpaAuthenticationEntity entity) {
        RadiusAuthProperties prop = entity.readProperty(RadiusAuthProperties.class);
        RadiusProperties l = new RadiusProperties();
        l.setFailoverOnException(prop.isFailoverOnException());
        l.setFailoverOnAuthenticationFailure(prop.isFailoverOnAuthenticationFailure());
        l.getClient().setInetAddress(prop.getInetAddress());
        l.getClient().setSharedSecret(prop.getSharedSecret());
        l.getClient().setAuthenticationPort(prop.getAuthenticationPort());
        l.getClient().setAccountingPort(prop.getAccountingPort());
        l.getClient().setSocketTimeout(prop.getSocketTimeout());
        l.getServer().setProtocol(prop.getProtocol());
        l.getServer().setRetries(prop.getRetries());
//        l.getPasswordEncoder().setType(prop.getPasswordEncoderType());
//        l.getPasswordEncoder().setEncodingAlgorithm(prop.getEncodingAlgorithm());
//        l.getPasswordEncoder().setSecret(prop.getSecret());
//        l.getPasswordEncoder().setStrength(prop.getStrength());
        l.setName(String.valueOf(entity.getId()));
        AbstractAuthenticationHandler handler = RadiusAuthenticatorFactory.radiusAuthenticationHandler(l, casSslContext, applicationContext, ldapPrincipalFactory, servicesManager);
        handler.setState(l.getState());
        authenticationEventExecutionPlan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver);
    }

    private void activeWeibo(JpaAuthenticationEntity entity) {
        OAuthAuthProperties cmd = entity.readProperty();
        WeiboClient client = new WeiboClient(cmd.getClientId(), cmd.getSecret());
        client.setName(String.valueOf(entity.getId()));
        client.setCallbackUrl(casProperties.getServer().getPrefix() + "/callback/");
        client.setCallbackUrlResolver(new PathParameterCallbackUrlResolver());
        client.init();
        ((CustomRefreshableDelegatedClients) clients).addClient(client);
    }

    private void activeDingtalk(JpaAuthenticationEntity entity) {
        DingtalkAuthProperties cmd = entity.readProperty();
        DingTalkClient client = new DingTalkClient(cmd.getClientId(), cmd.getSecret());
        client.setName(String.valueOf(entity.getId()));
        client.setCallbackUrl(casProperties.getServer().getLoginUrl());
        client.init();
        ((CustomRefreshableDelegatedClients) clients).addClient(client);

        if (StringUtils.isNotBlank(cmd.getCropId())) {
            DingTalklessClient lessClient = new DingTalklessClient(cmd.getClientId(), cmd.getSecret(), cmd.getCropId());
            lessClient.setName(entity.getId() + "less");
            lessClient.setCallbackUrl(casProperties.getServer().getLoginUrl());
            lessClient.init();
            ((CustomRefreshableDelegatedClients) clients).addClient(lessClient);
        }
    }


    @Override
    public void load() {
        try {
            unload();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        loadInternal();
    }

    @Override
    public void unload() throws IllegalAccessException {
        ((CustomRefreshableDelegatedClients) clients).clearClient();

        final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlerPrincipalResolverMap = reflectAuthenticationHandlerMap();
        authenticationHandlerPrincipalResolverMap.keySet().removeIf(e -> {
            if (e instanceof LdapAuthenticationHandler) {
                if (e instanceof DisposableBean) {
                    try {
                        ((LdapAuthenticationHandler) e).destroy();
                    } catch (Exception ignore) {
                    }
                }
                return true;
            }
            if (e instanceof RadiusAuthenticationHandler) {
                return true;
            }
            return false;
        });
    }

    private Map<AuthenticationHandler, PrincipalResolver> reflectAuthenticationHandlerMap() throws IllegalAccessException {
        final Field field = ReflectionUtils.findField(DefaultAuthenticationEventExecutionPlan.class,
                "authenticationHandlerPrincipalResolverMap");
        field.setAccessible(true);
        final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlerPrincipalResolverMap =
                (Map<AuthenticationHandler, PrincipalResolver>) field.get(
                        authenticationEventExecutionPlan);
        return authenticationHandlerPrincipalResolverMap;
    }

    @Override
    public void afterPropertiesSet() {
        loadInternal();
    }

    private void loadInternal() {
        Collection<JpaAuthenticationEntity> query = jpaAuthenticationHandlerRegistry.query();
        for (JpaAuthenticationEntity entity : query) {
            try {
                this.activeAuthn(entity);
            } catch (Exception e) {
                LOGGER.error("loadInternal {} exception", entity.getName(), e);
            }
        }
    }


}
