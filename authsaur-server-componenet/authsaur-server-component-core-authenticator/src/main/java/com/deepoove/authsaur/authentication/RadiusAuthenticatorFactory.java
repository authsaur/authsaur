package com.deepoove.authsaur.authentication;

import lombok.val;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.apereo.cas.adaptors.radius.server.AbstractRadiusServer;
import org.apereo.cas.adaptors.radius.server.NonBlockingRadiusServer;
import org.apereo.cas.adaptors.radius.server.RadiusServerConfigurationContext;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusServerProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;

public class RadiusAuthenticatorFactory {

    static Set<String> getClientIps(final RadiusClientProperties client) {
        return StringUtils.commaDelimitedListToSet(StringUtils.trimAllWhitespace(client.getInetAddress()));
    }

    private static AbstractRadiusServer getSingleRadiusServer(final RadiusClientProperties client,
                                                              final RadiusServerProperties server,
                                                              final String clientInetAddress,
                                                              final CasSSLContext casSSLContext) {
        val factory = RadiusClientFactory.builder()
                .authenticationPort(client.getAccountingPort())
                .authenticationPort(client.getAuthenticationPort())
                .socketTimeout(client.getSocketTimeout())
                .inetAddress(clientInetAddress)
                .sharedSecret(client.getSharedSecret())
                .sslContext(casSSLContext)
                .transportType(client.getTransportType())
                .build();
        val protocol = RadiusProtocol.valueOf(server.getProtocol());
        val context = RadiusServerConfigurationContext.builder()
                .protocol(protocol)
                .radiusClientFactory(factory)
                .retries(server.getRetries())
                .nasIpAddress(server.getNasIpAddress())
                .nasIpv6Address(server.getNasIpv6Address())
                .nasPort(server.getNasPort())
                .nasPortId(server.getNasPortId())
                .nasIdentifier(server.getNasIdentifier())
                .nasRealPort(server.getNasRealPort())
                .nasPortType(server.getNasPortType())
                .build();
        return new NonBlockingRadiusServer(context);
    }

    public static AbstractRadiusServer radiusServer(final RadiusProperties radius,
                                                    final CasSSLContext casSslContext) {
        val client = radius.getClient();
        val server = radius.getServer();
        val ips = getClientIps(client);
        return getSingleRadiusServer(client, server, ips.iterator().next(), casSslContext);
    }


    public static RadiusAuthenticationHandler radiusAuthenticationHandler(
            final RadiusProperties radius,
            final CasSSLContext casSslContext,
            final ConfigurableApplicationContext applicationContext,
            final PrincipalFactory radiusPrincipalFactory,
            final ServicesManager servicesManager) {
        AbstractRadiusServer radiusServer = radiusServer(radius, casSslContext);
        val h = new RadiusAuthenticationHandler(radius.getName(), servicesManager,
                radiusPrincipalFactory, Arrays.asList(radiusServer), radius.isFailoverOnException(),
                radius.isFailoverOnAuthenticationFailure());
        h.setState(radius.getState());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(radius.getPasswordEncoder(), applicationContext));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(radius.getPrincipalTransformation()));
        h.setPasswordPolicyConfiguration(new PasswordPolicyContext());
        return h;
    }

}
