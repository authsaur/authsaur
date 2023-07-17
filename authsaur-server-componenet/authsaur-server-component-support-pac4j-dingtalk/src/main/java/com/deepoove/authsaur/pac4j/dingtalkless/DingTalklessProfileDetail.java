package com.deepoove.authsaur.pac4j.dingtalkless;

import org.pac4j.oauth.profile.OAuth20Profile;

import java.net.URI;

public class DingTalklessProfileDetail extends OAuth20Profile {

    private static final long serialVersionUID = -8030906034414268058L;

    @Override
    public String getDisplayName() {
        return (String) getAttribute(DingTalklessProfileDetailDefinition.NICK);
    }

    @Override
    public String getUsername() {
        return (String) getAttribute(DingTalklessProfileDetailDefinition.NICK);
    }

    @Override
    public URI getPictureUrl() {
        return (URI) getAttribute(DingTalklessProfileDetailDefinition.AVATARURL);
    }

    @Override
    public String getEmail() {
        return (String) getAttribute(DingTalklessProfileDetailDefinition.EMAIL);
    }

    @Override
    public String getId() {
        return (String) getAttribute(DingTalklessProfileDetailDefinition.UNIONID);
    }
}

