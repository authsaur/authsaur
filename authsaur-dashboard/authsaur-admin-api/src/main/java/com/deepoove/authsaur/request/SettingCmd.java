package com.deepoove.authsaur.request;

import lombok.Data;

import java.util.Map;

@Data
public class SettingCmd {

    private String key;

    private Map<String, String> value;
}
