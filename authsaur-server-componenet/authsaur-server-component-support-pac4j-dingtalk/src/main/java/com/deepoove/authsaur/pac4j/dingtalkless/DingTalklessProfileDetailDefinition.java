package com.deepoove.authsaur.pac4j.dingtalkless;

import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import org.pac4j.core.exception.AccountNotFoundException;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.converter.Converters;
import org.pac4j.oauth.config.OAuthConfiguration;
import org.pac4j.oauth.profile.JsonHelper;
import org.pac4j.oauth.profile.definition.OAuthProfileDefinition;

import java.util.Arrays;

import static org.pac4j.core.profile.AttributeLocation.PROFILE_ATTRIBUTE;

public class DingTalklessProfileDetailDefinition extends OAuthProfileDefinition {

    public static final String NICK = "name";
    public static final String AVATARURL = "avatar";
    public static final String MOBILE = "mobile";
    public static final String OPENID = "openId";
    public static final String UNIONID = "unionid";
    public static final String EMAIL = "email";
    public static final String STATECODE = "state_code";

    public DingTalklessProfileDetailDefinition() {
        super(x -> new DingTalklessProfile());
        Arrays.asList(new String[]{
                NICK, AVATARURL, MOBILE, OPENID, UNIONID, LOCATION, EMAIL, STATECODE
        }).forEach(a -> primary(a, Converters.STRING));
    }

    @Override
    public String getProfileUrl(Token accessToken, OAuthConfiguration configuration) {
        return "https://oapi.dingtalk.com/topapi/v2/user/get";
    }

    @Override
    public Verb getProfileVerb() {
        return Verb.POST;
    }

    @Override
    public DingTalklessProfileDetail extractUserProfile(final String body) {
        final var profile = new DingTalklessProfileDetail();
        var json = JsonHelper.getFirstNode(body);
        if (json != null) {
            var errcode = (Integer) JsonHelper.getElement(json, "errcode");
            if (errcode != null && errcode > 0) {
                var errmsg = JsonHelper.getElement(json, "errmsg");
                throw new AccountNotFoundException(
                        errmsg != null ? errmsg.toString() : "error code " + errcode);
            }
            json = json.get("result");
            profile.setId(ProfileHelper.sanitizeIdentifier(JsonHelper.getElement(json, "unionid")));
            for (final var attribute : getPrimaryAttributes()) {
                convertAndAdd(profile, PROFILE_ATTRIBUTE, attribute,
                        JsonHelper.getElement(json, attribute));
            }
        } else {
            raiseProfileExtractionJsonError(body);
        }
        return profile;
    }
}
