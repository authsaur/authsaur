package com.deepoove.authsaur.event;

import com.deepoove.authsaur.config.AuthsaurRedisEventTriggerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class RedisStreamEventTrigger implements AuthsaurEventTrigger {

    final StringRedisTemplate template;

    @Override
    public void trigger(AuthsaurEvent event) {
        LOGGER.info("trigger event:{}", event);
        String name = event.name();
        Map<String, String> map = new HashMap<>();
        map.put("object", name);
        StringRecord record = StreamRecords.string(map).withStreamKey(
                AuthsaurRedisEventTriggerConfiguration.TOPIC);
        StreamOperations streamOperations = template.opsForStream();
        streamOperations.trim(AuthsaurRedisEventTriggerConfiguration.TOPIC, 20);
        streamOperations.add(record);
    }

}
