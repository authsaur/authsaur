package com.deepoove.authsaur.event;

import com.deepoove.authsaur.authentication.AuthenticatorManager;
import com.deepoove.authsaur.config.AuthsaurRedisEventListenerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import com.deepoove.authsaur.setting.SettingsConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

import java.util.Map;

@Slf4j
public class RedisStreamEventListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private SettingsConf settingsConf;

    @Autowired
    private AuthenticatorManager authenticatorManager;

    public RedisStreamEventListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {

        try {
            Map<String, String> value = message.getValue();
            LOGGER.info("MessageId: {}", message.getId());
            LOGGER.info("Stream: {}", message.getStream());
            LOGGER.info("Body: {}", value);
            String object = value.get("object");
            AuthsaurEvent authsaurEvent = AuthsaurEvent.valueOf(object);
            if (authsaurEvent == AuthsaurEvent.SERVICE) {
                servicesManager.load();
            } else if (authsaurEvent == AuthsaurEvent.SETTING) {
                settingsConf.clear();
            } else if (authsaurEvent == AuthsaurEvent.AUTHN) {
                authenticatorManager.load();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.redisTemplate.opsForStream().acknowledge(AuthsaurRedisEventListenerConfiguration.TOPIC, message);
        }
    }


}
