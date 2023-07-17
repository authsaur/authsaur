package com.deepoove.authsaur.setting;

import com.google.gson.Gson;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsConf {

    private Map<String, Map<String, Object>> cache = new ConcurrentHashMap();

    public void clear() {
        cache.clear();
    }

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SettingsConf(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
    }

    static Gson gson = new Gson();

    /**
     * read from cache first
     *
     * @param key
     * @return
     */
    public Map<String, Object> getSetting(String key) {
        Map<String, Object> stringObjectMap = cache.get(key);
        if (null != stringObjectMap) {
            return stringObjectMap;
        }
        String sql = "SELECT value FROM CAS_SETTINGS where name = ?";
        String value = jdbcTemplate.queryForObject(sql, String.class, key);
        if (null == value) return Collections.emptyMap();
        Map map = gson.fromJson(value, Map.class);
        cache.put(key, map);
        return map;
    }

    public Map<String, Map<String, Object>> getSettings(List<String> keys) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", keys);
        String sql = "SELECT * FROM CAS_SETTINGS where name in (:name)";
        List<Map<String, Object>> maps = namedParameterJdbcTemplate.queryForList(sql, params);
        Map<String, Map<String, Object>> ret = new HashMap<>();
        for (Map<String, Object> map : maps) {
            Object name = map.get("name");
            Object value = map.get("value");
            ret.put(name.toString(), gson.fromJson(value.toString(), Map.class));
        }
        return ret;
    }

    public void saveSetting(String key, Object value) {
        String sql = "UPDATE CAS_SETTINGS SET value=? WHERE name=?";
        String val = gson.toJson(value);
        jdbcTemplate.update(sql, val, key);
        cache.remove(key);
    }
}
