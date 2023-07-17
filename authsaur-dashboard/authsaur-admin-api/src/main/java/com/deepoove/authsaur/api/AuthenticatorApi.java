package com.deepoove.authsaur.api;

import com.deepoove.authsaur.authenticator.*;
import com.deepoove.authsaur.event.AuthsaurEvent;
import com.deepoove.authsaur.event.AuthsaurEventTrigger;
import com.deepoove.authsaur.exception.AuthenticatorExistException;
import com.deepoove.authsaur.exception.AuthenticatorNotFoundException;
import com.deepoove.authsaur.jpa.*;
import com.deepoove.authsaur.request.AuthenticatorCmd;
import com.deepoove.authsaur.request.StateCmd;
import com.deepoove.authsaur.response.AuthenticatorDTO;
import com.deepoove.authsaur.response.BypassUserDTO;
import com.deepoove.authsaur.response.Result;
import com.deepoove.authsaur.util.JSONHelper;
import com.deepoove.authsaur.util.MockFieldUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/authenticator")
public class AuthenticatorApi {

    @Autowired
    private JpaAuthenticationHandlerRegistry jpaAuthRegistry;
    @Autowired
    private JpaUserRegistry jpaUserRegistry;
    @Autowired
    private JpaOrgRegistry jpaOrgRegistry;
    @Autowired
    private AuthsaurEventTrigger eventTrigger;

    @GetMapping
    public Result<Map<String, List<AuthenticatorDTO>>> query() {
        return Result.successWithList(jpaAuthRegistry.query().stream().map(entity -> {
            AuthenticatorDTO dto = new AuthenticatorDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public Result<AuthenticatorDTO> get(@PathVariable String id) {
        JpaAuthenticationEntity entity = check(id);
        AuthenticatorDTO dto = new AuthenticatorDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setConfig(populateOtpResponse(entity));
        return Result.success(dto);
    }

    private Map<String, Object> populateOtpResponse(JpaAuthenticationEntity entity) {
        Map<String, Object> config = JSONHelper.toMap(entity.getBody());
        mock(config);
        if (AuthenticatorType.MFA_OTP.getType().equals(entity.getType())) {
            MFAAuthProperties properties = entity.readProperty();
            List<String> bypassUserIds = properties.getBypassUserIds();
            if (null != bypassUserIds) {
                config.put("bypassUsers",
                        jpaUserRegistry.queryByPrincipalList(bypassUserIds).stream()
                                .map(u -> new BypassUserDTO(u.getPrincipal(), u.getName()))
                                .collect(Collectors.toList()));
            }
        }
        return config;
    }

    private void mock(Map<String, Object> config) {
        if (config.containsKey("bindCredential")) {
            config.put("bindCredential", MockFieldUtils.mockField(config.get("bindCredential").toString()));
        }
        if (config.containsKey("sharedSecret")) {
            config.put("sharedSecret", MockFieldUtils.mockField(config.get("sharedSecret").toString()));
        }
        if (config.containsKey("secret")) {
            config.put("secret", MockFieldUtils.mockField(config.get("secret").toString()));
        }
    }

    @PutMapping("/{id}")
    public Result<Void> state(@PathVariable String id, @RequestBody StateCmd cmd) {
        JpaAuthenticationEntity handler = check(id);
        if (cmd.isEnabled()) {
            handler.setState(AuthenticationHandlerStates.ACTIVE.toString());
        } else {
            handler.setState(AuthenticationHandlerStates.STANDBY.toString());
        }
        jpaAuthRegistry.saveOrUpdate(handler);
        eventTrigger.trigger(AuthsaurEvent.AUTHN);
        return Result.success();
    }


    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        check(id);
        jpaAuthRegistry.delete(id);
        eventTrigger.trigger(AuthsaurEvent.AUTHN);
        return Result.success();
    }

    @PostMapping("saveOrUpdateLdap")
    public Result<JpaAuthenticationEntity> saveOrUpdateLdap(@RequestBody AuthenticatorCmd<LdapAuthProperties> cmd) {
        JpaAuthenticationEntity checked = check("-1".equals(cmd.getId()) ? null : cmd.getId());
        if (null != checked) {
            LdapAuthProperties p = checked.readProperty();
            if (MockFieldUtils.isMockField(cmd.getProperties().getBindCredential())) {
                cmd.getProperties().setBindCredential(p.getBindCredential());
            }
        }
        return Result.success(newAuthenticationEntity(cmd, true, e -> {
            LdapAuthProperties prop = cmd.getProperties();
            String filterTemplate = prop.getSearchFilterTemplate();
            String id = prop.getPrincipalAttributeId();
            prop.setSearchFilter(filterTemplate.replaceFirst("%s", id).replaceFirst("%s", "user"));
            if (StringUtils.isNotBlank(prop.getEmailAttribute())) {
                prop.setEmailSearchFilter(filterTemplate.replaceAll("%s", prop.getEmailAttribute()));
            }
            if (StringUtils.isNotBlank(prop.getPhoneAttribute())) {
                prop.setPhoneSearchFilter(filterTemplate.replaceAll("%s", prop.getPhoneAttribute()));
            }
            e.fromProperty(prop);
            e.setType(AuthenticatorType.LDAP.getType());
        }));
    }
    @PostMapping("saveOrUpdateRadius")
    public Result<JpaAuthenticationEntity> saveOrUpdateRadius(@RequestBody AuthenticatorCmd<RadiusAuthProperties> cmd) {
        JpaAuthenticationEntity checked = check("-1".equals(cmd.getId()) ? null : cmd.getId());
        if (null != checked) {
            RadiusAuthProperties p = checked.readProperty();
            if (MockFieldUtils.isMockField(cmd.getProperties().getSharedSecret())) {
                cmd.getProperties().setSharedSecret(p.getSharedSecret());
            }
//            if (MockFieldUtils.isMockField(cmd.getProperties().getSecret())) {
//                cmd.getProperties().setSecret(p.getSecret());
//            }
        }
        return Result.success(newAuthenticationEntity(cmd, true, e -> {
            e.setType(AuthenticatorType.RADIUS.getType());
            e.setRelatedAuthnId(null);
            e.setRelatedWithMail(false);
            e.setRelatedWithPhone(false);
        }));
    }

    @PostMapping("saveOrUpdateWeibo")
    public Result<JpaAuthenticationEntity> saveOrUpdateWeibo(@RequestBody AuthenticatorCmd<OAuthAuthProperties> cmd) {
        check("-1".equals(cmd.getId()) ? null : cmd.getId());
        return Result.success(newAuthenticationEntity(cmd, false, e -> e.setType(AuthenticatorType.WEIBO.getType())));
    }

    @PostMapping("saveOrUpdateDingTalk")
    public Result<JpaAuthenticationEntity> saveOrUpdateDingTalk(@RequestBody AuthenticatorCmd<DingtalkAuthProperties> cmd) {
        JpaAuthenticationEntity checked = check("-1".equals(cmd.getId()) ? null : cmd.getId());
        if (null != checked) {
            DingtalkAuthProperties p = checked.readProperty();
            if (MockFieldUtils.isMockField(cmd.getProperties().getSecret())) {
                cmd.getProperties().setSecret(p.getSecret());
            }
        }
        return Result.success(newAuthenticationEntity(cmd, true, e -> e.setType(AuthenticatorType.DINGTALK.getType())));
    }

    @PostMapping("saveOrUpdateOTP")
    public Result<JpaAuthenticationEntity> saveOrUpdateOTP(@RequestBody AuthenticatorCmd<OTPAuthProperties> cmd) {
        String unique = "mfa-gauth";
        checkUnique(unique);
        JpaAuthenticationEntity entity = new JpaAuthenticationEntity();
        entity.setId(unique);
        entity.setName(cmd.getName());
        OTPAuthProperties properties = cmd.getProperties();
        entity.fromProperty(properties);
        entity.setBodyType(cmd.getProperties().getClass().getName());
        entity.setType(AuthenticatorType.MFA_OTP.getType());
        entity.setState(cmd.getState());
        entity.setRelatedAuthnId(null);
        entity.setRelatedWithMail(false);
        entity.setRelatedWithPhone(false);
        jpaAuthRegistry.saveOrUpdate(entity);
        return Result.success(entity);
    }


    private JpaAuthenticationEntity newAuthenticationEntity(AuthenticatorCmd<?> cmd, boolean createOrg,
                                                            Consumer<JpaAuthenticationEntity> processor) {
        JpaAuthenticationEntity entity = new JpaAuthenticationEntity();
        entity.setId(cmd.getId());
        entity.setName(cmd.getName());
        entity.setState(cmd.getState());
        entity.setRelatedAuthnId(cmd.getRelatedAuthnId());
        entity.setRelatedWithMail(cmd.isRelatedWithMail());
        entity.setRelatedWithPhone(cmd.isRelatedWithPhone());
        entity.fromProperty(cmd.getProperties());
        entity.setBodyType(cmd.getProperties().getClass().getName());
        processor.accept(entity);
        jpaAuthRegistry.saveOrUpdate(entity);
        if (createOrg) {
            // auto create user org directory
            autoCreateOrganization(entity);
        }
        eventTrigger.trigger(AuthsaurEvent.AUTHN);
        return entity;
    }

    private JpaAuthenticationEntity check(String id) {
        if (null == id) return null;
        JpaAuthenticationEntity original = jpaAuthRegistry.findById(id);
        if (original == null) {
            throw new AuthenticatorNotFoundException(id);
        }
        return original;
    }


    private void checkUnique(String unique) {
        if ("-1".equals(unique)) {
            JpaAuthenticationEntity byId = jpaAuthRegistry.findById(unique);
            if (null != byId) {
                throw new AuthenticatorExistException(unique);
            }
        }
    }

    private void autoCreateOrganization(JpaAuthenticationEntity entity) {
        JpaOrgEntity org = jpaOrgRegistry.findRootBySource(entity.getId());
        if (null == org) {
            org = new JpaOrgEntity();
            org.setId(UUID.randomUUID().toString().substring(0, 4));
            org.setName(entity.getName());
            org.setSource(entity.getId());
            org.setParentId(null);
            org.setPath("/" + org.getId());
            org.setCreated(new Date());
            org.setUpdated(new Date());
            org.setType(entity.getType());
            jpaOrgRegistry.saveOrUpdate(org);
        }
    }


}
