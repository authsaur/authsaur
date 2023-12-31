package com.deepoove.authsaur.pac4j.dingtalk;

import com.github.scribejava.core.utils.OAuthEncoder;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.credentials.extractor.OAuth20CredentialsExtractor;
import org.pac4j.oauth.exception.OAuthCredentialsException;

import java.util.Optional;

public class DingTalkOAuth20CredentialsExtractor extends OAuth20CredentialsExtractor {

    public DingTalkOAuth20CredentialsExtractor(final OAuth20Configuration configuration, final IndirectClient client) {
        super(configuration, client);
    }

     @Override
    protected Optional<Credentials> getOAuthCredentials(final WebContext context, final SessionStore sessionStore) {
        if (((OAuth20Configuration) configuration).isWithState()) {

            final var stateParameter = context.getRequestParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER);

            if (stateParameter.isPresent()) {
                final var stateSessionAttributeName = this.client.getStateSessionAttributeName();
                final var sessionState = (String) sessionStore.get(context, stateSessionAttributeName).orElse(null);
                // clean from session after retrieving it
                sessionStore.set(context, stateSessionAttributeName, null);
                logger.debug("sessionState: {} / stateParameter: {}", sessionState, stateParameter);
                if (!stateParameter.get().equals(sessionState)) {
                    final var message = "State parameter mismatch: session expired or possible threat of cross-site request forgery";
                    throw new OAuthCredentialsException(message);
                }
            } else {
                final var message = "Missing state parameter: session expired or possible threat of cross-site request forgery";
                throw new OAuthCredentialsException(message);
            }

        }

        final var codeParameter = context.getRequestParameter("authCode");
        if (codeParameter.isPresent()) {
            final var code = OAuthEncoder.decode(codeParameter.get());
            logger.debug("code: {}", code);
            return Optional.of(new OAuth20Credentials(code));
        } else {
            final var message = "No credential found";
            throw new OAuthCredentialsException(message);
        }
    }
}
