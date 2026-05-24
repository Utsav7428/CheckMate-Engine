package com.checkmate.websocket;

import com.checkmate.websocket.dto.ClientMessage;
import com.checkmate.websocket.dto.ServerMessage;
import com.checkmate.service.GameCoordinatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GameCoordinatorService coordinatorService;

    private final Map<String, Sinks.Many<ServerMessage>> matchChannels = new ConcurrentHashMap<>();

    public GameWebSocketHandler(GameCoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<ClientMessage> inboundStream = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::decodeClientMessage)
                .doOnNext(msg -> handleClientEvent(msg, session))
                .doOnError(err -> log.error("WebSocket inbound stream failure", err));

        return inboundStream.thenEmpty(Mono.empty());
    }

    public void broadcastToMatch(String matchId, ServerMessage message) {
        Sinks.Many<ServerMessage> sink = matchChannels.get(matchId);
        if (sink != null) {
            sink.tryEmitNext(message);
        }
    }

    private void handleClientEvent(ClientMessage message, WebSocketSession session) {
        String matchId = message.matchId();

        if (message.type() == ClientMessage.Type.JOIN) {
            log.info("Player {} handshaking with match {}", message.playerId(), matchId);

            matchChannels.putIfAbsent(matchId, Sinks.many().multicast().onBackpressureBuffer());
            Sinks.Many<ServerMessage> sink = matchChannels.get(matchId);

            session.send(sink.asFlux()
                    .map(this::encodeServerMessage)
                    .map(session::textMessage)
            ).subscribe();

            broadcastToMatch(matchId, new ServerMessage(ServerMessage.Type.SYSTEM, matchId, null, "Player joined"));
        }

        if (message.type() == ClientMessage.Type.MOVE) {
            log.info("Received move request from player {} in match {}", message.playerId(), matchId);

            coordinatorService.processPlayerMove(matchId, message.playerId(), message.move())
                    .subscribe(
                            updatedSession -> broadcastToMatch(matchId, ServerMessage.update(matchId, updatedSession.gameState())),
                            error -> session.send(Mono.just(session.textMessage(encodeServerMessage(
                                    ServerMessage.error(matchId, error.getMessage())
                            )))).subscribe()
                    );
        }
    }

    private ClientMessage decodeClientMessage(String json) {
        try {
            return objectMapper.readValue(json, ClientMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Malformed WebSocket frame received", e);
        }
    }

    private String encodeServerMessage(ServerMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize server frame to JSON", e);
        }
    }
}