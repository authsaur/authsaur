package com.deepoove.authsaur.pac4j.dingtalk;

import org.pac4j.oauth.profile.OAuth20Profile;

import java.net.URI;

public class DingTalkProfile extends OAuth20Profile {

    private static final long serialVersionUID = -8030906034414268058L;

    @Override
    public String getDisplayName() {
        return (String) getAttribute(DingTalkProfileDefinition.NICK);
    }

    @Override
    public String getUsername() {
        return (String) getAttribute(DingTalkProfileDefinition.NICK);
    }

    @Override
    public URI getPictureUrl() {
        return (URI) getAttribute(DingTalkProfileDefinition.AVATARURL);
    }

    @Override
    public String getEmail() {
        return (String) getAttribute(DingTalkProfileDefinition.EMAIL);
    }

    @Override
    public String getId() {
        return (String) getAttribute(DingTalkProfileDefinition.UNIONID);
    }
}

