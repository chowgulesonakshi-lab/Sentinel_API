package org.example.realtime;

import org.example.entity.AppUser;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RealtimeEventService {

    private final ConcurrentMap<String, Set<SseEmitter>> emittersByEmail = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String email) {
        String ownerKey = normalizeEmail(email);
        if (ownerKey == null) {
            throw new IllegalArgumentException("An authenticated user email is required");
        }

        SseEmitter emitter = new SseEmitter(0L);
        emittersByEmail.computeIfAbsent(ownerKey, ignored -> ConcurrentHashMap.newKeySet())
                .add(emitter);

        Runnable cleanup = () -> removeEmitter(ownerKey, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("connected", true)));
        } catch (Exception exception) {
            cleanup.run();
            emitter.completeWithError(exception);
        }

        return emitter;
    }

    public void publishMonitoringUpdate(Endpoint endpoint, Metrics metrics) {
        if (endpoint == null || endpoint.getApiService() == null
                || endpoint.getApiService().getProject() == null
                || endpoint.getApiService().getProject().getOwner() == null) {
            return;
        }

        AppUser owner = endpoint.getApiService().getProject().getOwner();
        String ownerKey = normalizeEmail(owner.getEmail());
        if (ownerKey == null) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("endpointId", endpoint.getId());
        payload.put("success", metrics.isSuccess());
        payload.put("status", metrics.getStatus() == null ? null : metrics.getStatus().name());
        payload.put("statusCode", metrics.getStatusCode());
        payload.put("responseTime", metrics.getResponseTime());
        payload.put("timestamp", metrics.getTimeStamp() == null
                ? null : metrics.getTimeStamp().toString());

        Set<SseEmitter> emitters = emittersByEmail.get(ownerKey);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("monitoring-update")
                        .data(payload));
            } catch (Exception exception) {
                removeEmitter(ownerKey, emitter);
                emitter.completeWithError(exception);
            }
        }
    }

    @Scheduled(fixedRate = 20000)
    public void sendHeartbeats() {
        emittersByEmail.forEach((ownerKey, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("keep-alive"));
                } catch (Exception exception) {
                    removeEmitter(ownerKey, emitter);
                    emitter.completeWithError(exception);
                }
            }
        });
    }

    private void removeEmitter(String ownerKey, SseEmitter emitter) {
        emittersByEmail.computeIfPresent(ownerKey, (key, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
