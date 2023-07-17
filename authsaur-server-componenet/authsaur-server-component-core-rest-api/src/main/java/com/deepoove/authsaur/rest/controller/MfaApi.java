package com.deepoove.authsaur.rest.controller;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.rest.interceptor.AuthHolder;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import com.deepoove.authsaur.rest.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/mfa/", produces = {"application/json;charset=UTF-8"})
public class MfaApi {

    @Autowired
    CasConfigurationProperties casProperties;

    @Autowired
    OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @DeleteMapping
    public Result<Boolean> unbind() {
        Principal user = AuthHolder.getUser();
        googleAuthenticatorAccountRegistry.delete(user.getId());
        return Result.success(true);
    }


}
