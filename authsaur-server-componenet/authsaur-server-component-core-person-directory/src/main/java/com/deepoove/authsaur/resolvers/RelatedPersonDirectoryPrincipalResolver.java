package com.deepoove.authsaur.resolvers;

import com.deepoove.authsaur.person.PersonUtils;
import com.deepoove.authsaur.person.PersonManager;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalException;
import com.deepoove.authsaur.authenticator.AuthenticatorType;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.principal.MinePrincipal;
import com.deepoove.authsaur.principal.PrincipalAttr;
import com.deepoove.authsaur.jpa.JpaUserEntity;
import com.deepoove.authsaur.jpa.JpaUserRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.IPersonAttributeDao;

import javax.security.auth.login.FailedLoginException;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@ToString
@Getter
@Setter
public class RelatedPersonDirectoryPrincipalResolver extends PersonDirectoryPrincipalResolver {

    private final PrincipalResolutionContext context;
    private CasConfigurationProperties casProperties;
    private JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;
    private JpaUserRegistry jpaUserRegistry;
    private PersonManager personManager;

    public RelatedPersonDirectoryPrincipalResolver(PrincipalResolutionContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> currentPrincipal,
                             final Optional<AuthenticationHandler> handler) {

        LOGGER.debug("Atemp to result a principal via [{}]", getName());
        var principalId = extractPrincipalId(credential, currentPrincipal);
        if (StringUtils.isBlank(principalId)) {
            LOGGER.debug("Principal id [{}] could not be found", principalId);
            return null;
        }
        if (context.getPrincipalNameTransformer() != null) {
            principalId = context.getPrincipalNameTransformer().transform(principalId);
        }
        LOGGER.debug("Creating principal for [{}]", principalId);

        String authId = PersonUtils.getAuthId(credential, handler);
        if (StringUtils.isBlank(authId)) {
            return null;
        }

        JpaAuthenticationEntity authenticator = jpaAuthenticationHandlerRegistry.findById(authId);
        if (null == authenticator.getRelatedAuthnId()) {
            LOGGER.debug("Not Related auth [{}]", authId);
            return null;
        }
        if (currentPrincipal.isEmpty()) {
            LOGGER.debug("currentPrincipal is null [{}]", principalId);
            return null;
        }
        JpaAuthenticationEntity relatedAuthenticator = jpaAuthenticationHandlerRegistry.findById(
                authenticator.getRelatedAuthnId());
        val attributes = retrievePersonAttributes(authenticator, relatedAuthenticator, principalId, credential,
                currentPrincipal, new HashMap<>(0));
        if (attributes == null || attributes.isEmpty()) {
            LOGGER.debug("Principal id [{}] did not related any user", principalId);
            //TODO 没有关联用户
            throw new UnresolvedPrincipalException(
                    new FailedLoginException("No related records found for user " + principalId));
        }
        LOGGER.debug("Retrieved [{}] attribute(s) from the repository", attributes);

        // find related principal and find correct id
        PrincipalResolutionResult result = convertPersonAttributesToPrincipal(relatedAuthenticator, principalId,
                currentPrincipal,
                attributes);
        if (!result.isSuccess() && context.isReturnNullIfNoAttributes()) {
            LOGGER.warn("Principal resolution is unable to produce a result and will return null");
            //TODO 没有关联用户
            throw new UnresolvedPrincipalException(
                    new FailedLoginException("No related records found for user " + principalId));
        }

        // save related person
        MinePrincipal resolve = new MinePrincipal(result.getPrincipalId(), result.getAttributes());
        JpaUserEntity userEntity = personManager.saveOrUpdatePrincipal(resolve, relatedAuthenticator);

        Principal currentUser = currentPrincipal.orElse(null);
        currentUser.getAttributes().put(PrincipalAttr.AUTH_ID.getAttr(), Arrays.asList(new Object[]{authenticator.getId()}));
        currentUser.getAttributes().put(PrincipalAttr.AUTH_TYPE.getAttr(), Arrays.asList(new Object[]{authenticator.getType()}));
        resolve.getAttributes().put("urelated", Arrays.asList(new Object[]{currentUser.getAttributes()}));
        PersonUtils.populateAttributes(resolve, relatedAuthenticator, userEntity,
                personManager.getAllDepts(userEntity.getPrincipal()));

        val principal = buildResolvedPrincipal(userEntity.getPrincipal(), resolve.getAttributes(), credential,
                currentPrincipal, handler);
        LOGGER.debug("Final resolved principal by [{}] is [{}]", getName(), principal);
        return principal;
    }


    protected Map<String, List<Object>> retrievePersonAttributes(final JpaAuthenticationEntity currentEntity,
                                                                 final JpaAuthenticationEntity relatedEntity,
                                                                 final String principalId,
                                                                 final Credential credential,
                                                                 final Optional<Principal> currentPrincipal,
                                                                 final Map<String, List<Object>> queryAttributes) {

        queryAttributes.computeIfAbsent("credentialId", k1 -> CollectionUtils.wrapList(credential.getId()));
        queryAttributes.computeIfAbsent("credentialClass",
                k -> CollectionUtils.wrapList(credential.getClass().getSimpleName()));

        IPersonAttributeDao dao = null;

        Map<String, List<Object>> retrieve = null;
        if (AuthenticatorType.LDAP.getType().equals(relatedEntity.getType())) {
            if (null != currentEntity.getRelatedWithMail() && currentEntity.getRelatedWithMail()) {
                dao = PersonUtils.getLdaptivePersonAttributeDao(currentEntity, relatedEntity, "mail");
                if (null != dao) {
                    retrieve = queryFromRepo(principalId, currentPrincipal, queryAttributes, dao);
                    if (null != retrieve && !retrieve.isEmpty()) return retrieve;
                }
            }
            if (null != currentEntity.getRelatedWithPhone() && currentEntity.getRelatedWithPhone()) {
                dao = PersonUtils.getLdaptivePersonAttributeDao(currentEntity, relatedEntity, "phone");
                if (null != dao) {
                    return queryFromRepo(principalId, currentPrincipal, queryAttributes, dao);
                }
            }
        } else if (AuthenticatorType.PASSWORD.getType().equals(relatedEntity.getType())) {
            if (null != currentEntity.getRelatedWithMail() && currentEntity.getRelatedWithMail()) {
                String emailAttr = PersonUtils.getEmailAttr(currentEntity);
                if (StringUtils.isNotBlank(emailAttr) && currentPrincipal.isPresent()) {
                    Map<String, List<Object>> p = currentPrincipal.get().getAttributes();
                    String email = null;
                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(p.get(emailAttr))) {
                        email = String.valueOf(p.get(emailAttr).get(0));
                    }
                    if (null != email) {
                        JpaUserEntity user = jpaUserRegistry.findByEmail(email, relatedEntity.getId());
                        if (null != user) {
                            return convert2Map(user);
                        }
                    }
                }
            }
            if (null != currentEntity.getRelatedWithPhone() && currentEntity.getRelatedWithPhone()) {
                String phoneAttr = PersonUtils.getPhoneAttr(currentEntity);
                if (StringUtils.isNotBlank(phoneAttr) && currentPrincipal.isPresent()) {
                    Map<String, List<Object>> p = currentPrincipal.get().getAttributes();
                    String phone = null;
                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(p.get(phoneAttr))) {
                        phone = String.valueOf(p.get(phoneAttr).get(0));
                    }
                    if (null != phone) {
                        JpaUserEntity user = jpaUserRegistry.findByMobile(phone, relatedEntity.getId());
                        if (null != user) {
                            return convert2Map(user);
                        }
                    }
                }
            }
        }
        return null;

    }

    private Map<String, List<Object>> queryFromRepo(String principalId, Optional<Principal> currentPrincipal,
                                                    Map<String, List<Object>> queryAttributes,
                                                    IPersonAttributeDao dao) {
        return PrincipalAttributeRepositoryFetcher.builder().attributeRepository(dao).principalId(
                principalId).activeAttributeRepositoryIdentifiers(
                context.getActiveAttributeRepositoryIdentifiers()).currentPrincipal(
                currentPrincipal.orElse(null)).queryAttributes(queryAttributes).build().retrieve();
    }

    private Map<String, List<Object>> convert2Map(JpaUserEntity user) {
        Map<String, List<Object>> attrs = new HashMap<>();
        attrs.put("userId", CollectionUtils.wrapList(user.getUserId()));
        attrs.put("email", CollectionUtils.wrapList(user.getEmail()));
        attrs.put("mobile", CollectionUtils.wrapList(user.getMobile()));
        attrs.put("name", CollectionUtils.wrapList(user.getName()));
        return attrs;
    }


    protected PrincipalResolutionResult convertPersonAttributesToPrincipal(final JpaAuthenticationEntity relatedEntity,
                                                                           final String extractedPrincipalId,
                                                                           final Optional<Principal> currentPrincipal
            , final Map<String, List<Object>> attributes) {
        val convertedAttributes = new LinkedHashMap<String, List<Object>>();
        attributes.forEach((key, attrValue) -> {
            val values = ((List<Object>) CollectionUtils.toCollection(attrValue, ArrayList.class)).stream().filter(
                    Objects::nonNull).collect(toList());
            LOGGER.debug("Found attribute [{}] with value(s) [{}]", key, values);
            convertedAttributes.put(key, values);
        });

        val builder = PrincipalResolutionResult.builder();
        var principalId = extractedPrincipalId;

        val attrNames = new HashSet<String>();
        attrNames.add(PersonUtils.getIDAttr(relatedEntity));


        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(attrNames)) {
//            val attrNames = org.springframework.util.StringUtils.commaDelimitedListToSet(context
//            .getPrincipalAttributeNames());


            val principalIdAttributes = new LinkedHashMap<>(attributes);
            if (context.isUseCurrentPrincipalId() && currentPrincipal.isPresent()) {
                val currentPrincipalAttributes = currentPrincipal.get().getAttributes();
                LOGGER.trace("Merging current principal attributes [{}] with resolved attributes [{}]",
                        currentPrincipalAttributes, principalIdAttributes);
                context.getAttributeMerger().mergeAttributes(principalIdAttributes, currentPrincipalAttributes);
            }

            LOGGER.debug("Using principal attributes [{}] to determine principal id", principalIdAttributes);
            val result = attrNames.stream().map(String::trim).filter(principalIdAttributes::containsKey).map(
                    principalIdAttributes::get).findFirst();

            if (result.isEmpty()) {
                LOGGER.warn(
                        "Principal resolution is set to resolve users via attribute(s) [{}], and yet " + "the " +
                                "collection of attributes retrieved [{}] do not contain any of those attributes" +
                                "." + " This is " + "likely due to misconfiguration and CAS will use [{}] as the " +
                                "final " + "principal id",
                        context.getPrincipalAttributeNames(), principalIdAttributes.keySet(), principalId);
                builder.success(false);
            } else {
                val values = result.get();
                if (!values.isEmpty()) {
                    principalId = CollectionUtils.firstElement(values).map(Object::toString).orElseThrow();
                    LOGGER.debug("Found principal id attribute value [{}]", principalId);
                }
            }
        }
        return builder.principalId(principalId).attributes(convertedAttributes).build();
    }

    @SuperBuilder
    @Getter
    static class PrincipalResolutionResult {
        private final String principalId;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new HashMap<>();

        @Builder.Default
        private final boolean success = true;
    }

}
