package com.deepoove.authsaur.style;

import com.deepoove.authsaur.setting.SettingKey;
import lombok.extern.slf4j.Slf4j;
import com.deepoove.authsaur.setting.SettingsConf;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.Map;

@Slf4j
public class AuthsaurMessageBundle implements MessageSource {

    private SettingsConf settingsConf;

    public AuthsaurMessageBundle(SettingsConf settingsConf) {
        this.settingsConf = settingsConf;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return getMessageIntenal(code, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return getMessageIntenal(code, locale);
    }


    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return null;
    }


    private String getMessageIntenal(String code, Locale locale) {
        if (null == code || !code.startsWith(SettingKey.PREFIX)) return null;
        String field = code.substring(code.lastIndexOf(".") + 1);
        if (code.startsWith(SettingKey.WORD)) {
            field = locale.toString() + field;
        }
        String key = code.substring(0, code.lastIndexOf("."));
        Map<String, Object> stringObjectMap = settingsConf.getSetting(key);
        Object orDefault = stringObjectMap.getOrDefault(field, null);
//        LOGGER.info(key + "   " + field + "   " + orDefault);
        return null == orDefault ? null : orDefault.toString();
    }
}
