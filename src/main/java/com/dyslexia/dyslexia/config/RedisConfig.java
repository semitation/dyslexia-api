package com.dyslexia.dyslexia.config;

import com.dyslexia.dyslexia.service.RedisMessageSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Value("${redis.channels.progress}")
    private String progressChannel;

    @Value("${redis.channels.result}")
    private String resultChannel;

    @Value("${redis.channels.failure}")
    private String failureChannel;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            @Qualifier("progressMessageListener") MessageListenerAdapter progressMessageListener,
            @Qualifier("resultMessageListener") MessageListenerAdapter resultMessageListener,
            @Qualifier("failureMessageListener") MessageListenerAdapter failureMessageListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(progressMessageListener, new ChannelTopic(progressChannel));
        container.addMessageListener(resultMessageListener, new ChannelTopic(resultChannel));
        container.addMessageListener(failureMessageListener, new ChannelTopic(failureChannel));

        return container;
    }

    @Bean
    public MessageListenerAdapter progressMessageListener(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleProgressMessage");
    }

    @Bean
    public MessageListenerAdapter resultMessageListener(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleResultMessage");
    }

    @Bean
    public MessageListenerAdapter failureMessageListener(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleFailureMessage");
    }
}
