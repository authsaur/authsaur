package com.deepoove.authsaur.person;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import com.deepoove.authsaur.authenticator.AuthenticatorType;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.authenticator.LdapAuthProperties;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import com.deepoove.authsaur.principal.PrincipalAttr;
import com.deepoove.authsaur.jpa.JpaUserEntity;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchResultHandler;

import javax.naming.directory.SearchControls;
import java.util.*;

@Slf4j
public class PersonUtils {

    private static final LdapEntryHandler[] LDAP_ENTRY_HANDLERS = new LdapEntryHandler[0];

    private static final SearchResultHandler[] SEARCH_RESULT_HANDLERS = new SearchResultHandler[0];

    public static String getIDAttr(JpaAuthenticationEntity entity) {
        if (entity.getType().equals(AuthenticatorType.LDAP.getType())) {
            LdapAuthProperties p = entity.readProperty();
            return p.getPrincipalAttributeId();
        } else if (entity.getType().equals(AuthenticatorType.PASSWORD.getType())) {
            return "userId";
        } else {
            throw new IllegalStateException(entity.getType());
        }
    }

    public static String getEmailAttr(JpaAuthenticationEntity currentEntity) {
        if (AuthenticatorType.LDAP.getType().equals(currentEntity.getType())) {
            LdapAuthProperties o = currentEntity.readProperty();
            return o.getEmailAttribute();
        } else if (AuthenticatorType.DINGTALK.getType().equals(currentEntity.getType())) {
            return "email";
        } else if (AuthenticatorType.PASSWORD.getType().equals(currentEntity.getType())) {
            return "email";
        }
        return null;
    }

    public static String getPhoneAttr(JpaAuthenticationEntity currentEntity) {
        if (AuthenticatorType.LDAP.getType().equals(currentEntity.getType())) {
            LdapAuthProperties o = currentEntity.readProperty();
            return o.getPhoneAttribute();
        } else if (AuthenticatorType.DINGTALK.getType().equals(currentEntity.getType())) {
            return "mobile";
        } else if (AuthenticatorType.PASSWORD.getType().equals(currentEntity.getType())) {
            return "mobile";
        }
        return null;
    }

    public static String getAuthId(Credential credential, Optional<AuthenticationHandler> handler) {
        String name = null;
        if (handler.isPresent()) {
            String authenticationName = handler.get().getName();
            if (credential instanceof ClientCredential) {
                String clientName = ((ClientCredential) credential).getClientName();
                name = clientName.endsWith("less") ? clientName.substring(0,
                        clientName.length() - "less".length()) : clientName;
            } else {
                name = authenticationName;
            }
        }

        return name;
    }

    public static void populateAttributes(Principal resolve, JpaAuthenticationEntity authenticator,
                                          JpaUserEntity userEntity, Set<String> depts) {
        resolve.getAttributes().put(PrincipalAttr.NAME.getAttr(), org.apereo.cas.util.CollectionUtils.wrapList(userEntity.getName()));
        resolve.getAttributes().put(PrincipalAttr.MAIL.getAttr(), org.apereo.cas.util.CollectionUtils.wrapList(userEntity.getEmail()));
        resolve.getAttributes().put(PrincipalAttr.PHONE.getAttr(), org.apereo.cas.util.CollectionUtils.wrapList(userEntity.getMobile()));
        resolve.getAttributes().put(PrincipalAttr.USER_ID.getAttr(), Arrays.asList(new Object[]{userEntity.getUserId()}));
        resolve.getAttributes().put(PrincipalAttr.AUTH_ID.getAttr(), Arrays.asList(new Object[]{authenticator.getId()}));
        resolve.getAttributes().put(PrincipalAttr.AUTH_TYPE.getAttr(), Arrays.asList(new Object[]{authenticator.getType()}));
        resolve.getAttributes().put(PrincipalAttr.PRINCIPAL.getAttr(), Arrays.asList(new Object[]{userEntity.getPrincipal()}));
        resolve.getAttributes().put(PrincipalAttr.DEPT.getAttr(), new ArrayList<>(depts));
    }

    public static IPersonAttributeDao getLdaptivePersonAttributeDao(JpaAuthenticationEntity currentEntity,
                                                                    JpaAuthenticationEntity entity, String daoType) {
        // create attribute repository
        LdapAuthProperties config = entity.readProperty();
        LdapAuthenticationProperties ldap = entity.readProperty(LdapAuthenticationProperties.class);
        ldap.setName(String.valueOf(entity.getId()));
        val dao = new LdaptivePersonAttributeDao();
        FunctionUtils.doIfNotNull(ldap.getName(), dao::setId);
        LOGGER.debug("Configured LDAP attribute source for [{}] and baseDn [{}]", ldap.getLdapUrl(), ldap.getBaseDn());
        dao.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap));
        dao.setBaseDN(ldap.getBaseDn());
//                    dao.setEnabled(ldap.getState() != AttributeRepositoryStates.DISABLED);
        dao.putTag(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName(), true);


        val constraints = new SearchControls();
//                    if (ldap.getAttributes() != null && !ldap.getAttributes().isEmpty()) {
//                        LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", ldap.getLdapUrl(),
//                        ldap.getAttributes());
//                        dao.setResultAttributeMapping(ldap.getAttributes());
//                        val attributes = (String[]) ldap.getAttributes().keySet().toArray(ArrayUtils
//                        .EMPTY_STRING_ARRAY);
//                        constraints.setReturningAttributes(attributes);
//                    } else {
        LOGGER.debug("Retrieving all attributes as no explicit attribute mappings are defined for [{}]",
                ldap.getLdapUrl());
        constraints.setReturningAttributes(null);
//                    }

        val binaryAttributes = ldap.getBinaryAttributes();
        if (binaryAttributes != null && !binaryAttributes.isEmpty()) {
            LOGGER.debug("Setting binary attributes [{}]", binaryAttributes);
            dao.setBinaryAttributes(binaryAttributes.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }

        val searchEntryHandlers = ldap.getSearchEntryHandlers();
        if (searchEntryHandlers != null && !searchEntryHandlers.isEmpty()) {
            val entryHandlers = LdapUtils.newLdaptiveEntryHandlers(searchEntryHandlers);
            if (!entryHandlers.isEmpty()) {
                LOGGER.debug("Setting entry handlers [{}]", entryHandlers);
                dao.setEntryHandlers(entryHandlers.toArray(LDAP_ENTRY_HANDLERS));
            }
            val searchResultHandlers = LdapUtils.newLdaptiveSearchResultHandlers(searchEntryHandlers);
            if (!searchResultHandlers.isEmpty()) {
                LOGGER.debug("Setting search result handlers [{}]", searchResultHandlers);
                dao.setSearchResultHandlers(searchResultHandlers.toArray(SEARCH_RESULT_HANDLERS));
            }
        }

        if (ldap.isSubtreeSearch()) {
            LOGGER.debug("Configured subtree searching for [{}]", ldap.getLdapUrl());
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        }
        Map<String, String> queryAttributeMapping = new HashMap<>();
        if ("mail".equals(daoType)) {
            LOGGER.debug("LDAP attributes are fetched from [{}] via filter [{}]", ldap.getLdapUrl(),
                    config.getEmailAttribute());
            if (StringUtils.isBlank(config.getEmailSearchFilter())) {
                return null;
            }
            dao.setSearchFilter(config.getEmailSearchFilter());
            String emailAttribute = config.getEmailAttribute();
            String emailAttr = getEmailAttr(currentEntity);
            if (StringUtils.isNotBlank(emailAttr) && StringUtils.isNotBlank(emailAttribute)) {
                queryAttributeMapping.put(emailAttr, emailAttribute);
            }
        }
        if ("phone".equals(daoType)) {
            LOGGER.debug("LDAP attributes are fetched from [{}] via filter [{}]", ldap.getLdapUrl(),
                    config.getPhoneAttribute());
            if (StringUtils.isBlank(config.getPhoneSearchFilter())) {
                return null;
            }
            dao.setSearchFilter(config.getPhoneSearchFilter());
            String phoneAttribute = config.getPhoneAttribute();
            String phoneAttr = getPhoneAttr(currentEntity);
            if (StringUtils.isNotBlank(phoneAttr) && StringUtils.isNotBlank(phoneAttribute)) {
                queryAttributeMapping.put(phoneAttr, phoneAttribute);
            }
        }
        if (!queryAttributeMapping.isEmpty()) {
            dao.setQueryAttributeMapping(queryAttributeMapping);
        } else {
            return null;
        }
        constraints.setDerefLinkFlag(true);
        dao.setSearchControls(constraints);
//                    dao.setUseAllQueryAttributes(ldap.isUseAllQueryAttributes());
        dao.setUseAllQueryAttributes(false);
//        dao.setOrder(ldap.getOrder());
        LOGGER.debug("Adding LDAP attribute source for [{}]", ldap.getLdapUrl());
        return dao;
    }


}
