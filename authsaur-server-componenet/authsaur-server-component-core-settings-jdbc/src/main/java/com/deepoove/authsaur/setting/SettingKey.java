package com.deepoove.authsaur.setting;

public interface SettingKey {

    String PREFIX = "authsaur.";

    String CONSOLE = PREFIX + "console";
    String CONSOLE_SLO = "SLO";

    String SAFE = PREFIX + "safe";
    String SAFE_AUDIT_MAX_AGE_DAY = "auditMaxAgeDay";
    String SAFE_PRINCIPAL_POLICY = "principalPolicy";
    String SAFE_PASSWORD_POLICY_PATTERN = "passwordPolicyPattern";
    String SAFE_MAX_TIME_TOLIVEINSECONDS = "maxTimeToLiveInSeconds";
    String SAFE_TIME_TOKILLINSECONDS = "timeToKillInSeconds";

    String THEME = PREFIX + "theme";
    String THEME_COLOR = "color";

    String WORD = PREFIX + "i18n";
}
