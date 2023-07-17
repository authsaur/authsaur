package com.deepoove.authsaur.pac4j.dingtalk;

import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.credentials.authenticator.OAuth20Authenticator;
import org.pac4j.oauth.redirect.OAuth20RedirectionActionBuilder;

public class DingTalkClient extends OAuth20Client {

    public static final String DEFAULT_SCOPE = "openid";

    public DingTalkClient() {
        setScope(DEFAULT_SCOPE);
    }

    public DingTalkClient(final String key, final String secret) {
        setScope(DEFAULT_SCOPE);
        setKey(key);
        setSecret(secret);
    }

    @Override
    protected void internalInit(final boolean forceReinit) {
        configuration.setApi(DingTalkApi.instance());
        configuration.setProfileDefinition(new DingTalkProfileDefinition());
        configuration.setTokenAsHeader(true);
//        defaultLogoutActionBuilder((ctx, profile, targetUrl) ->
//                Optional.of(RedirectionActionHelper.buildRedirectUrlAction(ctx, "")));


        defaultRedirectionActionBuilder(new OAuth20RedirectionActionBuilder(configuration, this));
        defaultCredentialsExtractor(new DingTalkOAuth20CredentialsExtractor(configuration, this));
        defaultAuthenticator(new OAuth20Authenticator(configuration, this));
        defaultProfileCreator(new DingTalkOAuth20ProfileCreator(configuration, this));
        super.internalInit(forceReinit);
    }

    public String getScope() {
        return getConfiguration().getScope();
    }

    public void setScope(final String scope) {
        getConfiguration().setScope(scope);
    }
}

