package com.checkmate.matchmaking;

import com.checkmate.matchmaking.dto.MatchCreatedEvent;
import com.checkmate.matchmaking.dto.PlayerRegistrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MatchmakingConsumer {

    private static final Logger log = LoggerFactory.getLogger(MatchmakingConsumer.class);
    private static final String MATCHMAKING_TOPIC = "chess-matchmaking";
    private static final String MATCH_CREATED_TOPIC = "chess-match-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Thread-safe in-memory buckets per Kafka partition to hold waiting players
    private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PlayerRegistrationEvent>> waitingRooms = new ConcurrentHashMap<>();

    public MatchmakingConsumer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = MATCHMAKING_TOPIC, groupId = "matchmaking-group")
    public void processRegistration(PlayerRegistrationEvent event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        waitingRooms.putIfAbsent(partition, new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<PlayerRegistrationEvent> queue = waitingRooms.get(partition);

        // Prevent a user from queuing up multiple times by mashing the button
        if (queue.stream().noneMatch(p -> p.playerId().equals(event.playerId()))) {
            queue.add(event);
            log.info("Player {} joined queue for partition {}", event.playerId(), partition);
        }

        // If we have at least 2 players in this Elo band, pop them and create a match
        if (queue.size() >= 2) {
            PlayerRegistrationEvent player1 = queue.poll();
            PlayerRegistrationEvent player2 = queue.poll();

            if (player1 != null && player2 != null) {
                String matchId = UUID.randomUUID().toString();
                int avgElo = (player1.eloRating() + player2.eloRating()) / 2;

                MatchCreatedEvent matchEvent = new MatchCreatedEvent(
                        matchId,
                        player1.playerId(), // Assigned White
                        player2.playerId(), // Assigned Black
                        avgElo
                );

                // Broadcast the created match to initialize the game session
                kafkaTemplate.send(MATCH_CREATED_TOPIC, matchId, matchEvent);
                log.info("Match {} created between {} and {}", matchId, player1.playerId(), player2.playerId());
            }
        }
    }
}