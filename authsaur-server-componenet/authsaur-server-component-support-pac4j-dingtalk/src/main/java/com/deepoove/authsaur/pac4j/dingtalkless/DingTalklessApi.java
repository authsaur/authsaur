package com.deepoove.authsaur.pac4j.dingtalkless;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.AccessTokenRequestParams;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.utils.Preconditions;
import lombok.Getter;
import org.pac4j.oauth.profile.JsonHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class DingTalklessApi extends DefaultApi20 {

    protected DingTalklessApi() {
    }

    private static class InstanceHolder {
        private static final DingTalklessApi INSTANCE = new DingTalklessApi();
    }

    public static DingTalklessApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.GET;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://oapi.dingtalk.com/gettoken";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://login.dingtalk.com/oauth2/auth";
    }

    @Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return new OAuth2AccessTokenJsonExtractor() {
            public OAuth2AccessToken extract(Response response) throws IOException {
                final String body = response.getBody();
                Preconditions.checkEmptyString(body, "Response body is incorrect. Can't extract a token from an empty string");

                if (response.getCode() != 200) {
                    generateError(response);
                }
                final JsonNode responseNode = OBJECT_MAPPER.readTree(body);

                final JsonNode expiresInNode = responseNode.get("expires_in");
                final JsonNode refreshToken = null;
                final JsonNode scope = null;
                final JsonNode tokenType = null;

                return createToken(extractRequiredParameter(responseNode, "access_token", body).asText(),
                        tokenType == null ? null : tokenType.asText(), expiresInNode == null ? null : expiresInNode.asInt(),
                        refreshToken == null ? null : refreshToken.asText(), scope == null ? null : scope.asText(), responseNode,
                        body);
            }
        };
    }

    @Override
    public ClientAuthentication getClientAuthentication() {
        return new ClientAuthentication() {
            @Override
            public void addClientAuthentication(OAuthRequest request, String apiKey, String apiSecret) {
                request.addParameter("appkey", apiKey);
                if (apiSecret != null) {
                    request.addParameter("appsecret", apiSecret);
                }
            }
        };
    }

    @Override
    public OAuth20Service createService(String apiKey, String apiSecret, String callback, String defaultScope, String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        return new MyOAuth20Service(this, apiKey, apiSecret, callback, defaultScope, responseType, System.out, userAgent, httpClientConfig, httpClient);
    }

    @Getter
    public class MyOAuth20Service extends OAuth20Service {

        private String code;

        public MyOAuth20Service(DefaultApi20 api, String apiKey, String apiSecret, String callback, String defaultScope, String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
            super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
        }

        @Override
        protected OAuthRequest createAccessTokenRequest(AccessTokenRequestParams params) {
            final OAuthRequest request = new OAuthRequest(getAccessTokenVerb(), getAccessTokenEndpoint());

            getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());

            this.code = params.getCode();
//            request.addParameter(OAuthConstants.CODE, params.getCode());
//            final String callback = getCallback();
//            if (callback != null) {
//                request.addParameter(OAuthConstants.REDIRECT_URI, callback);
//            }
//
//            request.addParameter("grantType", OAuthConstants.AUTHORIZATION_CODE);
            if (isDebug()) {
                log("created access token request with body params [%s], query string params [%s]",
                        request.getBodyParams().asFormUrlEncodedString(),
                        request.getQueryStringParams().asFormUrlEncodedString());
            }
            request.addHeader("Content-Type", "application/json");
            ParameterList bodyParams = request.getBodyParams();
            Map<String, String> map = new HashMap<>();
            bodyParams.getParams().forEach(p -> map.put(p.getKey(), p.getValue()));
            request.setPayload(JsonHelper.toJSONString(map));
            return request;
        }
    }
}
