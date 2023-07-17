package com.deepoove.authsaur.pac4j.dingtalkless;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpCommunicationException;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.profile.creator.OAuth20ProfileCreator;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DingTalklessOAuth20ProfileCreator extends OAuth20ProfileCreator {


    public DingTalklessOAuth20ProfileCreator(OAuth20Configuration configuration, IndirectClient client) {
        super(configuration, client);
    }


    protected String sendRequestForDataNew(final OAuthService service, final Token accessToken, final String dataUrl,
                                           Verb verb, String authCode) {
        logger.debug("accessToken: {} / dataUrl: {}", accessToken, dataUrl);
        final var t0 = System.currentTimeMillis();
        final var request = createOAuthRequest(dataUrl, verb);
        request.addBodyParameter("code", authCode);
        final String body;
        final int code;
        try {
            var response = service.execute(request);
            code = response.getCode();
            body = response.getBody();
        } catch (final IOException | InterruptedException | ExecutionException e) {
            throw new HttpCommunicationException("Error getting body: " + e.getMessage());
        }
        final var t1 = System.currentTimeMillis();
        logger.debug("Request took: " + (t1 - t0) + " ms for: " + dataUrl);
        logger.debug("response code: {} / response body: {}", code, body);
        if (code != 200) {
            throw new HttpCommunicationException(code, body);
        }
        return body;
    }

    @Override
    protected Optional<UserProfile> retrieveUserProfileFromToken(final WebContext context, final Token accessToken) {
        final var profileDefinition = configuration.getProfileDefinition();
        final var profileUrl = profileDefinition.getProfileUrl(accessToken,
                configuration) + "?access_token=" + ((OAuth2AccessToken) accessToken).getAccessToken();
        final var service = this.configuration.buildService(context, client);
        String authCode = context.getRequestParameter("authCode").get();
        final var body = sendRequestForDataNew(service, accessToken, profileUrl, profileDefinition.getProfileVerb(),
                authCode);
        logger.info("UserProfile: " + body);
        if (body == null) {
            throw new HttpCommunicationException("No data found for accessToken: " + accessToken);
        }
        final UserProfile profile = configuration.getProfileDefinition().extractUserProfile(body);
//        addTokenToProfile(profile, accessToken);
        return retrieveUserProfileDetailFromToken(context, accessToken, (String) profile.getAttribute("userid"));
    }

    protected Optional<UserProfile> retrieveUserProfileDetailFromToken(final WebContext context,
                                                                       final Token accessToken, final String userId) {
        final var profileDefinition = new DingTalklessProfileDetailDefinition();
        final var profileUrl = profileDefinition.getProfileUrl(accessToken,
                configuration) + "?access_token=" + ((OAuth2AccessToken) accessToken).getAccessToken();
        final var service = this.configuration.buildService(context, client);
        final var body = sendRequestForData(service, accessToken, profileUrl, profileDefinition.getProfileVerb(),
                userId);
        logger.info("UserProfile: " + body);
        if (body == null) {
            throw new HttpCommunicationException("No data found for accessToken: " + accessToken);
        }
        final UserProfile profile = profileDefinition.extractUserProfile(body);
        addTokenToProfile(profile, accessToken);
        return Optional.of(profile);
    }

    protected String sendRequestForData(final OAuthService service, final Token accessToken, final String dataUrl,
                                        Verb verb, String userId) {
        logger.debug("accessToken: {} / dataUrl: {} / userId: {}", accessToken, dataUrl, userId);
        final var t0 = System.currentTimeMillis();
        final var request = createOAuthRequest(dataUrl, verb);

        final var token = ((OAuth2AccessToken) accessToken).getAccessToken();
//        request.addQuerystringParameter(OAuthConfiguration.OAUTH_TOKEN, token);
        request.addBodyParameter("userid", userId);
        request.addBodyParameter("language", "zh_CN");

        final String body;
        final int code;
        try {
            var response = service.execute(request);
            code = response.getCode();
            body = response.getBody();
        } catch (final IOException | InterruptedException | ExecutionException e) {
            throw new HttpCommunicationException("Error getting body: " + e.getMessage());
        }
        final var t1 = System.currentTimeMillis();
        logger.debug("Request took: " + (t1 - t0) + " ms for: " + dataUrl);
        logger.debug("response code: {} / response body: {}", code, body);
        if (code != 200) {
            throw new HttpCommunicationException(code, body);
        }
        return body;
    }


}
