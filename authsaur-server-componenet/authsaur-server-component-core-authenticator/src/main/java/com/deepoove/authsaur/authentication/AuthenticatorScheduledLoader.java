package com.deepoove.authsaur.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
@Slf4j
public class AuthenticatorScheduledLoader implements Runnable {
    private final AuthenticatorManager authenticatorManager;

    @Scheduled(
            initialDelayString = "${cas.custom.properties.authn-schedule-start-delay:PT20S}",
            fixedDelayString = "${cas.custom.properties.authn-schedule-repeat-interval:PT60S}"
    )
    @Override
    public void run() {
        try {
            LOGGER.debug("Reloading authenticator..");
            authenticatorManager.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
