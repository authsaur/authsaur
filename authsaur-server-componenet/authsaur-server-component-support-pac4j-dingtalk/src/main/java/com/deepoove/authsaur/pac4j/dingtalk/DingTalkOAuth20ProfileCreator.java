package com.deepoove.authsaur.pac4j.dingtalk;

import com.deepoove.authsaur.pac4j.dingtalkless.DingTalklessProfileDetailDefinition;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.AccountNotFoundException;
import org.pac4j.core.exception.HttpCommunicationException;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.config.OAuthConfiguration;
import org.pac4j.oauth.profile.JsonHelper;
import org.pac4j.oauth.profile.creator.OAuth20ProfileCreator;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DingTalkOAuth20ProfileCreator extends OAuth20ProfileCreator {


    public DingTalkOAuth20ProfileCreator(OAuth20Configuration configuration, IndirectClient client) {
        super(configuration, client);
    }

    @Override
    protected void signRequest(final OAuthService service, final Token token, final OAuthRequest request) {
        ((OAuth20Service) service).signRequest((OAuth2AccessToken) token, request);
        final var accessToken = ((OAuth2AccessToken) token).getAccessToken();
        if (this.configuration.isTokenAsHeader()) {
            request.addHeader("x-acs-dingtalk-access-token", accessToken);
        }
        if (Verb.POST.equals(request.getVerb())) {
            request.addParameter(OAuthConfiguration.OAUTH_TOKEN, accessToken);
        }
    }

    @Override
    protected Optional<UserProfile> retrieveUserProfileFromToken(WebContext context, Token accessToken) {
        Optional<UserProfile> userProfile = super.retrieveUserProfileFromToken(context, accessToken);
        String unionId = userProfile.get().getId();

        String token = retrieveToken(context);

        return retrieveUserProfileFromUnionId(context, new OAuth2AccessToken(token), unionId);
    }

    protected String retrieveToken(final WebContext context) {
        final var profileUrl =
                "https://oapi.dingtalk.com/gettoken?appkey=" + this.configuration.getKey() + "&appsecret=" + this.configuration.getSecret();
        final var service = this.configuration.buildService(context, client);
        final String body;
        final int code;
        try {
            var response = service.execute(createOAuthRequest(profileUrl, Verb.GET));
            code = response.getCode();
            body = response.getBody();
        } catch (final IOException | InterruptedException | ExecutionException e) {
            throw new HttpCommunicationException("Error getting body: " + e.getMessage());
        }
        final var t1 = System.currentTimeMillis();
        logger.debug("response code: {} / response body: {}", code, body);
        if (code != 200) {
            throw new HttpCommunicationException(code, body);
        }
        logger.info("TokenBody: " + body);
        if (body == null) {
            throw new HttpCommunicationException("No data found");
        }
        var json = JsonHelper.getFirstNode(body);
        if (json != null) {
            var errcode = (Integer) JsonHelper.getElement(json, "errcode");
            if (errcode != null && errcode > 0) {
                var errmsg = JsonHelper.getElement(json, "errmsg");
                throw new OAuthException(
                        errmsg != null ? errmsg.toString() : "error code " + errcode);
            }
            return ProfileHelper.sanitizeIdentifier(JsonHelper.getElement(json, "access_token"));
        } else {
            throw new TechnicalException("No JSON node to extract token from");
        }
    }

    protected Optional<UserProfile> retrieveUserProfileFromUnionId(final WebContext context, final Token accessToken,
                                                                   final String unionId) {
        final var profileUrl =
                "https://oapi.dingtalk.com/topapi/user/getbyunionid?access_token=" + ((OAuth2AccessToken) accessToken).getAccessToken();
        final var service = this.configuration.buildService(context, client);
        final var body = sendRequestForUnion(service, accessToken, profileUrl, Verb.POST, unionId);
        logger.info("UserIdProfile: " + body);
        if (body == null) {
            throw new HttpCommunicationException("No data found for accessToken: " + accessToken);
        }
        var json = JsonHelper.getFirstNode(body);
        if (json != null) {
            var errcode = (Integer) JsonHelper.getElement(json, "errcode");
            if (errcode != null && errcode > 0) {
                var errmsg = JsonHelper.getElement(json, "errmsg");
                throw new AccountNotFoundException(
                        errmsg != null ? errmsg.toString() : "error code " + errcode);
            }
            json = json.get("result");
            String userId = ProfileHelper.sanitizeIdentifier(JsonHelper.getElement(json, "userid"));
            return retrieveUserProfileDetailFromUser(context, accessToken, userId);
        } else {
            throw new TechnicalException("No JSON node to extract user profile from");
        }
    }

    protected Optional<UserProfile> retrieveUserProfileDetailFromUser(final WebContext context,
                                                                      final Token accessToken, final String userId) {
        final var profileDefinition = new DingTalklessProfileDetailDefinition();
        final var profileUrl = profileDefinition.getProfileUrl(accessToken,
                configuration) + "?access_token=" + ((OAuth2AccessToken) accessToken).getAccessToken();
        final var service = this.configuration.buildService(context, client);
        final var body = sendRequestForUser(service, accessToken, profileUrl, profileDefinition.getProfileVerb(),
                userId);
        logger.info("UserProfile: " + body);
        if (body == null) {
            throw new HttpCommunicationException("No data found for accessToken: " + accessToken);
        }
        final UserProfile profile = profileDefinition.extractUserProfile(body);
        addTokenToProfile(profile, accessToken);
        return Optional.of(profile);
    }

    protected String sendRequestForUnion(final OAuthService service, final Token accessToken, final String dataUrl,
                                         Verb verb, String unionid) {
        logger.debug("accessToken: {} / dataUrl: {} / unionid: {}", accessToken, dataUrl, unionid);
        final var t0 = System.currentTimeMillis();
        final var request = createOAuthRequest(dataUrl, verb);

        final var token = ((OAuth2AccessToken) accessToken).getAccessToken();
//        request.addQuerystringParameter(OAuthConfiguration.OAUTH_TOKEN, token);
        request.addBodyParameter("unionid", unionid);

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


    protected String sendRequestForUser(final OAuthService service, final Token accessToken, final String dataUrl,
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
