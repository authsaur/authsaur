package com.deepoove.authsaur.rest.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import com.deepoove.authsaur.rest.model.Result;
import com.deepoove.authsaur.rest.model.SendCommand;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.passwordless.PasswordlessAuthenticationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/pwdless", produces = {"application/json;charset=UTF-8"})
@Slf4j
public class PwdlessApi {

    @Autowired
    CasConfigurationProperties casProperties;

    @Autowired
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Autowired
    private CommunicationsManager communicationsManager;

    @PostMapping("sendCode")
    public Result<Boolean> sendCode(final HttpServletRequest request, @RequestBody SendCommand params) {

        PasswordlessAuthenticationProperties passwordlessProperties = casProperties.getAuthn().getPasswordless();
        String username = params.getUsername();
        val account = passwordlessUserAccountStore.findUser(username);
        if (account.isEmpty()) {
            LOGGER.error("Unable to locate passwordless user account for [{}]", username);
            return Result.fail("用户不存在");
        }
        val user = account.get();
        val token = passwordlessTokenRepository.createToken(user.getUsername());

        communicationsManager.validate();
        if (communicationsManager.isMailSenderDefined() && StringUtils.isNotBlank(user.getEmail())) {
            val mail = passwordlessProperties.getTokens().getMail();
            val body = EmailMessageBodyBuilder.builder().properties(mail)
                    .locale(Optional.ofNullable(request.getLocale()))
                    .parameters(Map.of("token", token)).build().produce();
            communicationsManager.email(mail, user.getEmail(), body);
        }
        if (communicationsManager.isSmsSenderDefined() && StringUtils.isNotBlank(user.getPhone())) {
            val smsProperties = passwordlessProperties.getTokens().getSms();
            communicationsManager.sms(smsProperties.getFrom(),
                    user.getPhone(), smsProperties.getFormattedText(token));
        }
        passwordlessTokenRepository.deleteTokens(user.getUsername());
        passwordlessTokenRepository.saveToken(user.getUsername(), token);

        return Result.success(true);
    }


}
