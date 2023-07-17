package com.deepoove.authsaur.config;


import com.deepoove.authsaur.event.RedisStreamEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;


@Configuration
public class AuthsaurRedisEventListenerConfiguration {

    public static final String TOPIC = "authsaur-stream";


    @Bean
    public RedisStreamEventListener streamListener(StringRedisTemplate redisTemplate) {
        return new RedisStreamEventListener(redisTemplate);
    }

    @Bean
    public StreamMessageListenerContainer StreamMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                         RedisStreamEventListener streamListener) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String,
                String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder().pollTimeout(Duration.ofMillis(1000)).build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        connectionFactory,
                        containerOptions);

        container.receive(StreamOffset.latest(TOPIC),
                streamListener);
        container.start();
        return container;
    }

}
