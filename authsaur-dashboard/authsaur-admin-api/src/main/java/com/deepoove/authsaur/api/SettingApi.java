package com.deepoove.authsaur.api;

import com.deepoove.authsaur.event.AuthsaurEvent;
import com.deepoove.authsaur.event.AuthsaurEventTrigger;
import com.deepoove.authsaur.request.SettingCmd;
import com.deepoove.authsaur.response.Result;
import com.deepoove.authsaur.setting.SettingsConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/setting")
public class SettingApi {

    @Autowired
    private SettingsConf settingsConf;

    @Autowired
    private AuthsaurEventTrigger eventTrigger;

    @GetMapping
    public Result<Map<String, Map<String, Object>>> getSettings(@RequestParam("keys") List<String> keys) {
        Map<String, Map<String, Object>> settings = settingsConf.getSettings(keys);
        return Result.success(settings);
    }

    @PutMapping
    public Result<Void> updateSettings(@RequestBody SettingCmd cmd) {
        settingsConf.saveSetting(cmd.getKey(), cmd.getValue());
        eventTrigger.trigger(AuthsaurEvent.SETTING);
        return Result.success();
    }

}
