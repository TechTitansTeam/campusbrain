package com.campusbrain.service;

import com.campusbrain.dto.RealtimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(SseBroadcastService.class);
    private static final Long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createConnection() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("SSE connection completed");
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE connection timed out");
            emitter.complete();
            emitters.remove(emitter);
        });

        emitter.onError(e -> {
            log.debug("SSE connection error: {}", e.getMessage());
            emitters.remove(emitter);
        });

        emitters.add(emitter);

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(new RealtimeEvent("CONNECTED", null, null, "Connected to CampusBrain live sync stream", null)));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(String eventType, Long materialId, String category, String message, Object payload) {
        RealtimeEvent event = new RealtimeEvent(eventType, materialId, category, message, payload);
        log.info("Broadcasting SSE event: {} for materialId: {}", eventType, materialId);

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(event));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }
}
