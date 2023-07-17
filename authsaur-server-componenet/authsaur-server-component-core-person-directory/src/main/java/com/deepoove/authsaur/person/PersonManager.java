package com.deepoove.authsaur.person;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.deepoove.authsaur.principal.PrincipalAttr;
import com.deepoove.authsaur.setting.PrincipalNamedPolicy;
import com.deepoove.authsaur.authenticator.AuthenticatorType;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.authenticator.LdapAuthProperties;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.jpa.JpaOrgEntity;
import com.deepoove.authsaur.jpa.JpaOrgRegistry;
import com.deepoove.authsaur.jpa.JpaOrgUserEntity;
import com.deepoove.authsaur.jpa.JpaOrgUserRegistry;
import com.deepoove.authsaur.jpa.JpaUserEntity;
import com.deepoove.authsaur.jpa.JpaUserRegistry;

import java.util.*;

@Slf4j
@AllArgsConstructor
public class PersonManager {

    private CasConfigurationProperties casProperties;
    private JpaUserRegistry jpaUserRegistry;
    private JpaOrgUserRegistry jpaOrgUserRegistry;
    private JpaOrgRegistry jpaOrgRegistry;
    private PrincipalNamedPolicy principalNamedPolicy;

    public Set<String> getAllDepts(String newPrincipal) {
        List<JpaOrgUserEntity> jpaOrgUserEntities = jpaOrgUserRegistry.queryOrgByPrincipal(newPrincipal);
        Set<String> orgSet = new HashSet<>();
        jpaOrgUserEntities.forEach(e -> {
            String orgPath = e.getOrgPath();
            String[] split = orgPath.split("/");
            Collections.addAll(orgSet, split);
        });
        orgSet.remove("");
        return orgSet;
    }

    public JpaUserEntity saveOrUpdatePrincipal(Principal resolve, JpaAuthenticationEntity authenticator) {
        List<Object> nameValue = guessNameValue(authenticator, resolve);
        List<Object> emailValue = guessEmailValue(authenticator, resolve);
        List<Object> phoneValue = guessPhoneValue(authenticator, resolve);

        // save or update user
        JpaUserEntity userEntity = jpaUserRegistry.findByUserSource(resolve.getId(), authenticator.getId());
        if (!AuthenticatorType.PASSWORD.getType().equals(authenticator.getType())) {
            if (null == userEntity) {
                String newPrincipal = principalNamedPolicy.named(resolve.getId(), authenticator.getId());
                userEntity = new JpaUserEntity();
                userEntity.setPrincipal(newPrincipal);
                userEntity.setUserId(resolve.getId());
                userEntity.setSource(authenticator.getId());
                userEntity.setCreated(new Date());
            }
            userEntity.setUpdated(new Date());
            if (CollectionUtils.isNotEmpty(nameValue)) {
                userEntity.setName(nameValue.get(0).toString());
            }
            if (CollectionUtils.isNotEmpty(emailValue)) {
                userEntity.setEmail(emailValue.get(0).toString());
            }
            if (CollectionUtils.isNotEmpty(phoneValue)) {
                userEntity.setMobile(phoneValue.get(0).toString());
            }
            LOGGER.info("save user:{}", userEntity);
            jpaUserRegistry.saveOrUpdate(userEntity);

            // save user-org
            List<JpaOrgUserEntity> jpaOrgUserEntities = jpaOrgUserRegistry.queryOrgByPrincipal(
                    userEntity.getPrincipal());
            if (CollectionUtils.isEmpty(jpaOrgUserEntities)) {
                List<JpaOrgUserEntity> userOrgs = new ArrayList<>();
                JpaOrgUserEntity orgUser = new JpaOrgUserEntity();
                orgUser.setPrincipal(userEntity.getPrincipal());
                JpaOrgEntity rootBySource = jpaOrgRegistry.findRootBySource(userEntity.getSource());
                orgUser.setOrgId(rootBySource.getId());
                orgUser.setOrgPath(rootBySource.getPath());
                userOrgs.add(orgUser);
                jpaOrgUserRegistry.saveOrUpdateOrgsByPrincipal(userOrgs);
            }
        }
        return userEntity;
    }

     private List<Object> guessNameValue(JpaAuthenticationEntity byId, Principal resolve) {
        String nameAttr = getNameAttr(byId);
        String uname = casProperties.getCustom().getProperties().get(PrincipalAttr.NAME.getAttr());
        String searchName = contact(nameAttr, uname);
        return readAttr(resolve, searchName);
    }

    private List<Object> guessEmailValue(JpaAuthenticationEntity byId, Principal resolve) {
        String attr = getEmailAttr(byId);
        String u = casProperties.getCustom().getProperties().get(PrincipalAttr.MAIL.getAttr());
        String searchName = contact(attr, u);
        return readAttr(resolve, searchName);
    }

    private List<Object> guessPhoneValue(JpaAuthenticationEntity byId, Principal resolve) {
        String attr = getPhoneAttr(byId);
        String u = casProperties.getCustom().getProperties().get(PrincipalAttr.PHONE.getAttr());
        String searchName = contact(attr, u);
        return readAttr(resolve, searchName);
    }




    private List<Object> readAttr(Principal resolve, String uattr) {
        if (null != resolve && null != uattr) {
            String[] split = uattr.split(",");
            for (String field : split) {
                if (null != resolve.getAttributes()) {
                    List<Object> objects = resolve.getAttributes().get(field);
                    if (null != objects && !objects.isEmpty()) {
                        return objects;
                    }
                }
            }
        }
        return null;
    }

    private String contact(String nameAttr, String uname) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(nameAttr)) {
            sb.append(nameAttr);
        }
        if (StringUtils.isNotBlank(uname)) {
            sb.append(",").append(uname);
        }
        return sb.toString();
    }

    private static String getNameAttr(JpaAuthenticationEntity currentEntity) {
        if (AuthenticatorType.LDAP.getType().equals(currentEntity.getType())) {
            LdapAuthProperties o = currentEntity.readProperty();
            return o.getNameAttribute();
        } else if (AuthenticatorType.DINGTALK.getType().equals(currentEntity.getType())) {
            return "name";
        } else if (AuthenticatorType.PASSWORD.getType().equals(currentEntity.getType())) {
            return "name";
        }
        return null;
    }

    private static String getEmailAttr(JpaAuthenticationEntity currentEntity) {
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

    private static String getPhoneAttr(JpaAuthenticationEntity currentEntity) {
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
}
