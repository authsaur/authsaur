package com.deepoove.authsaur.config;


import com.deepoove.authsaur.event.AuthsaurEventTrigger;
import com.deepoove.authsaur.event.RedisStreamEventTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;


@Configuration
public class AuthsaurRedisEventTriggerConfiguration {

    public static final String TOPIC = "authsaur-stream";

    @Bean
    public AuthsaurEventTrigger authsaurEventTrigger(StringRedisTemplate redisTemplate) {
        return new RedisStreamEventTrigger(redisTemplate);
    }


}
