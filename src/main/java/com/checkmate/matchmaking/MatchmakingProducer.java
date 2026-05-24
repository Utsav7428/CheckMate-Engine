package com.checkmate.matchmaking;

import com.checkmate.matchmaking.dto.PlayerRegistrationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchmakingProducer {

    private static final String MATCHMAKING_TOPIC = "chess-matchmaking";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EloPartitionStrategy partitionStrategy;

    public MatchmakingProducer(KafkaTemplate<String, Object> kafkaTemplate, EloPartitionStrategy partitionStrategy) {
        this.kafkaTemplate = kafkaTemplate;
        this.partitionStrategy = partitionStrategy;
    }

    public void queuePlayerForMatch(String playerId, int eloRating) {
        int partition = partitionStrategy.determinePartition(eloRating);
        PlayerRegistrationEvent event = new PlayerRegistrationEvent(playerId, eloRating, System.currentTimeMillis());

        // Send to specific partition based on Elo
        kafkaTemplate.send(MATCHMAKING_TOPIC, partition, playerId, event);
    }
}