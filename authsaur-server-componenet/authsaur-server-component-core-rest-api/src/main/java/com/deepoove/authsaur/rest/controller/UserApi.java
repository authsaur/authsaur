package com.deepoove.authsaur.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.rest.interceptor.AuthHolder;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import com.deepoove.authsaur.principal.MinePrincipal;
import com.deepoove.authsaur.rest.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/user/", produces = {"application/json;charset=UTF-8"})
@Slf4j
public class UserApi {

    @Autowired
    CasConfigurationProperties casProperties;

    @Autowired
    OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @GetMapping
    public Result<Object> current() {
        Principal user = AuthHolder.getUser();
        MinePrincipal principal = new MinePrincipal(user);
        Map<String, String> ret = new HashMap<>();
        ret.put("id", principal.getId());
        ret.put("userId", principal.getUserId());
        ret.put("name", principal.getName());
        ret.put("avatar", principal.getAvatar());
        return Result.success(ret);
    }


}
