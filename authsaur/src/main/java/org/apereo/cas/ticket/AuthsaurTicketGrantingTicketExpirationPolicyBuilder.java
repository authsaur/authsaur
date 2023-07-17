package org.apereo.cas.ticket;

import com.deepoove.authsaur.setting.SettingKey;
import com.deepoove.authsaur.setting.SettingsConf;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilder;

import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Slf4j
@Getter
public class AuthsaurTicketGrantingTicketExpirationPolicyBuilder extends TicketGrantingTicketExpirationPolicyBuilder {
    private static final long serialVersionUID = -4197980180617072826L;
    protected final SettingsConf settingsConf;

    public AuthsaurTicketGrantingTicketExpirationPolicyBuilder(CasConfigurationProperties casProperties,
                                                               SettingsConf settingsConf) {
        super(casProperties);
        this.settingsConf = settingsConf;
    }

    @Override
    protected ExpirationPolicy toTicketGrantingTicketExpirationPolicy() {
        Map<String, Object> setting = settingsConf.getSetting(SettingKey.SAFE);
        long maxTimeToLiveInSeconds = Long.parseLong(String.valueOf(setting.get(SettingKey.SAFE_MAX_TIME_TOLIVEINSECONDS)));
        long timeToKillInSeconds = Long.parseLong(String.valueOf(setting.get(SettingKey.SAFE_TIME_TOKILLINSECONDS)));
        LOGGER.debug("Ticket-granting ticket expiration policy is based on settings of [{}]/[{}] seconds",
                maxTimeToLiveInSeconds, timeToKillInSeconds);
        return new TicketGrantingTicketExpirationPolicy(maxTimeToLiveInSeconds, timeToKillInSeconds);
    }


}
