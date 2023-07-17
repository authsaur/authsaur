//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.deepoove.authsaur.audit;

import com.deepoove.authsaur.setting.SettingKey;
import com.deepoove.authsaur.setting.SettingsConf;
import org.apereo.inspektr.audit.support.AbstractWhereClauseMatchCriteria;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Calendar.DAY_OF_MONTH;

public class AuditWhereClauseMatchCriteria extends AbstractWhereClauseMatchCriteria {
    private static final String DATE_COLUMN = "AUD_DATE";
    protected SettingsConf settingsConf;

    public AuditWhereClauseMatchCriteria(SettingsConf settingsConf) {
        this.settingsConf = settingsConf;
        this.addCriteria(DATE_COLUMN, "<");
    }

    public List<?> getParameterValues() {
        Map<String, Object> setting = settingsConf.getSetting(SettingKey.SAFE);
        Object auditMaxAgeDay = setting.get(SettingKey.SAFE_AUDIT_MAX_AGE_DAY);
        int maxAge = Integer.parseInt(auditMaxAgeDay.toString());
        Calendar cal = Calendar.getInstance();
        cal.add(DAY_OF_MONTH, -maxAge);
        return Collections.singletonList(cal.getTime());
    }
}
