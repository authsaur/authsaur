package com.deepoove.authsaur.setting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
@Slf4j
public class SettingScheduledLoader implements Runnable {
    private final SettingsConf settingsConf;

    @Scheduled(
            initialDelayString = "${cas.custom.properties.setting-schedule-start-delay:PT20S}",
            fixedDelayString = "${cas.custom.properties.setting-schedule-repeat-interval:PT60S}"
    )
    @Override
    public void run() {
        try {
            LOGGER.debug("Reloading setting..");
            settingsConf.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
