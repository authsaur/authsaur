package com.deepoove.authsaur.resolvers;

import com.deepoove.authsaur.person.PersonUtils;
import com.deepoove.authsaur.person.PersonManager;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import com.deepoove.authsaur.jpa.JpaUserEntity;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Optional;

@Slf4j
@ToString
@AllArgsConstructor
public class AuthsaurEchoingPrincipalResolver implements PrincipalResolver {

    private JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;
    private PersonManager personManager;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> principal,
                             final Optional<AuthenticationHandler> handler) {
        Principal resolve = principal.orElse(null);
        if (null == resolve) return null;

        String authId = PersonUtils.getAuthId(credential, handler);
        if (StringUtils.isBlank(authId)) {
            LOGGER.warn("Cannot determine auth id, {},{}", credential, handler.orElse(null));
            return resolve;
        }
        JpaAuthenticationEntity authenticator = jpaAuthenticationHandlerRegistry.findById(authId);
        JpaUserEntity userEntity = personManager.saveOrUpdatePrincipal(resolve, authenticator);
        PersonUtils.populateAttributes(resolve, authenticator, userEntity,
                personManager.getAllDepts(userEntity.getPrincipal()));

        LOGGER.debug("Echoing back the authenticated principal [{}]", principal);
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(userEntity.getPrincipal(),
                resolve.getAttributes());
    }


    @Override
    public boolean supports(final Credential credential) {
        return StringUtils.isNotBlank(credential.getId());
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        return null;
    }


}

