package com.deepoove.authsaur.pac4j.dingtalk;

import com.github.scribejava.core.model.Token;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.converter.Converters;
import org.pac4j.oauth.config.OAuthConfiguration;
import org.pac4j.oauth.profile.JsonHelper;
import org.pac4j.oauth.profile.definition.OAuthProfileDefinition;

import java.util.Arrays;

import static org.pac4j.core.profile.AttributeLocation.PROFILE_ATTRIBUTE;

public class DingTalkProfileDefinition extends OAuthProfileDefinition {

    public static final String NICK = "nick";
    public static final String AVATARURL = "avatarUrl";
    public static final String MOBILE = "mobile";
    public static final String OPENID = "openId";
    public static final String UNIONID = "unionId";
    public static final String EMAIL = "email";
    public static final String STATECODE = "stateCode";

    public DingTalkProfileDefinition() {
        super(x -> new DingTalkProfile());
        Arrays.asList(new String[]{
                NICK, AVATARURL, MOBILE, OPENID, UNIONID, LOCATION, EMAIL, STATECODE
        }).forEach(a -> primary(a, Converters.STRING));
    }

    @Override
    public String getProfileUrl(Token accessToken, OAuthConfiguration configuration) {
        return "https://api.dingtalk.com/v1.0/contact/users/me";
    }


    @Override
    public DingTalkProfile extractUserProfile(final String body) {
        final var profile = (DingTalkProfile) newProfile();
        final var json = JsonHelper.getFirstNode(body);
        if (json != null) {
            profile.setId(ProfileHelper.sanitizeIdentifier(JsonHelper.getElement(json, "unionId")));
            for (final var attribute : getPrimaryAttributes()) {
                convertAndAdd(profile, PROFILE_ATTRIBUTE, attribute, JsonHelper.getElement(json, attribute));
            }
        } else {
            raiseProfileExtractionJsonError(body);
        }
        return profile;
    }
}
