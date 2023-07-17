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

public class DingTalklessProfileDefinition extends OAuthProfileDefinition {

    public static final String NICK = "name";
    public static final String USERID = "userid";
    public static final String UNIONID = "unionid";

    public DingTalklessProfileDefinition() {
        super(x -> new DingTalklessProfile());
        Arrays.asList(new String[]{
                NICK, UNIONID, USERID,
        }).forEach(a -> primary(a, Converters.STRING));
    }

    @Override
    public String getProfileUrl(Token accessToken, OAuthConfiguration configuration) {
        return "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo";
    }

    @Override
    public Verb getProfileVerb() {
        return Verb.POST;
    }

    @Override
    public DingTalklessProfile extractUserProfile(final String body) {
        final var profile = new DingTalklessProfile();
        var json = JsonHelper.getFirstNode(body);
        if (json != null) {
            var errcode = (Integer) JsonHelper.getElement(json, "errcode");
            if (errcode != null && errcode > 0) {
                var errmsg = JsonHelper.getElement(json, "errmsg");
                throw new AccountNotFoundException(
                        errmsg != null ? errmsg.toString() : "error code " + errcode);
            }
            json = json.get("result");
            profile.setId(ProfileHelper.sanitizeIdentifier(JsonHelper.getElement(json, "userid")));
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
