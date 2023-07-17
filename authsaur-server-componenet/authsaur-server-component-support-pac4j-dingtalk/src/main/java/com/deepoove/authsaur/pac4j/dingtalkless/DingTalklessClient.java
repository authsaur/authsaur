package com.deepoove.authsaur.pac4j.dingtalkless;

import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.credentials.authenticator.OAuth20Authenticator;
import org.pac4j.oauth.redirect.OAuth20RedirectionActionBuilder;

public class DingTalklessClient extends OAuth20Client {

    public static final String DEFAULT_SCOPE = "openid";

    private String cropId;

    public String getCropId() {
        return cropId;
    }

    public void setCropId(String cropId) {
        this.cropId = cropId;
    }

    public DingTalklessClient() {
        setScope(DEFAULT_SCOPE);
    }

    public DingTalklessClient(final String key, final String secret, final String cropId) {
        setScope(DEFAULT_SCOPE);
        setKey(key);
        setSecret(secret);
        this.cropId = cropId;
    }

    @Override
    protected void internalInit(final boolean forceReinit) {
        configuration.setApi(DingTalklessApi.instance());
        configuration.setProfileDefinition(new DingTalklessProfileDefinition());
        configuration.setTokenAsHeader(true);
//        defaultLogoutActionBuilder((ctx, profile, targetUrl) ->
//                Optional.of(RedirectionActionHelper.buildRedirectUrlAction(ctx, "")));


        defaultRedirectionActionBuilder(new OAuth20RedirectionActionBuilder(configuration, this));
        defaultCredentialsExtractor(new DingTalklessOAuth20CredentialsExtractor(configuration, this));
        defaultAuthenticator(new OAuth20Authenticator(configuration, this));
        defaultProfileCreator(new DingTalklessOAuth20ProfileCreator(configuration, this));
        super.internalInit(forceReinit);
    }

    public String getScope() {
        return getConfiguration().getScope();
    }

    public void setScope(final String scope) {
        getConfiguration().setScope(scope);
    }
}

