package com.deepoove.authsaur.setting;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static com.deepoove.authsaur.setting.SettingKey.SAFE_PRINCIPAL_POLICY;

@RequiredArgsConstructor
public class PrincipalNamedPolicy {

    final SettingsConf settingsConf;

    public String named(String principal, String authId) {
        Map<String, Object> setting = settingsConf.getSetting(SettingKey.SAFE);
        if ("global".equals(setting.get(SAFE_PRINCIPAL_POLICY))) {
            return principal;
        }
        return principal + "_" + UUID.randomUUID().toString().substring(0, 6);
//        return principal + "::" + authId;
    }
}
