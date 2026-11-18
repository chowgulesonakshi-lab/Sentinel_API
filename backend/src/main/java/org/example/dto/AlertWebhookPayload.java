package org.example.dto;

import java.time.LocalDateTime;

public record AlertWebhookPayload(
        long alertId,
        String event,
        String type,
        String severity,
        String message,
        boolean resolved,
        LocalDateTime occurredAt
) {
}
