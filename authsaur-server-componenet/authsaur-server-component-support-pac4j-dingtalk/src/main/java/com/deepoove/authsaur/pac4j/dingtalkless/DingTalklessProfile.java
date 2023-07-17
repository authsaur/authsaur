package com.deepoove.authsaur.pac4j.dingtalkless;

import org.pac4j.oauth.profile.OAuth20Profile;

public class DingTalklessProfile extends OAuth20Profile {

    private static final long serialVersionUID = -8030906034414268058L;

    @Override
    public String getDisplayName() {
        return (String) getAttribute(DingTalklessProfileDefinition.NICK);
    }

    @Override
    public String getUsername() {
        return (String) getAttribute(DingTalklessProfileDefinition.NICK);
    }

    @Override
    public String getId() {
        return (String) getAttribute(DingTalklessProfileDefinition.UNIONID);
    }
}

