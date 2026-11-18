package org.example.notification;

import org.example.dto.AlertWebhookPayload;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AlertWebhookNotifierTest {

    @Test
    void disabledWebhookDoesNotAttemptDelivery() {
        AlertWebhookNotifier notifier = new AlertWebhookNotifier(
                new RestTemplateBuilder(),
                false,
                ""
        );
        AlertWebhookPayload payload = new AlertWebhookPayload(
                42L,
                "ALERT_CREATED",
                "ENDPOINT_DOWN",
                "CRITICAL",
                "Endpoint is unavailable",
                false,
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> notifier.send(payload));
    }
}
