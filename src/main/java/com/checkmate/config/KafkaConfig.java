package com.checkmate.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String MATCHMAKING_TOPIC = "chess-matchmaking";
    public static final String MATCH_CREATED_TOPIC = "chess-match-created";

    @Bean
    public NewTopic matchmakingTopic() {
        // 6 partitions to allow parallel processing across different Elo bands
        return TopicBuilder.name(MATCHMAKING_TOPIC)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic matchCreatedTopic() {
        return TopicBuilder.name(MATCH_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}