package org.example.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.AlertWebhookPayload;
import org.example.security.EndpointUrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
@Slf4j
public class AlertWebhookNotifier {

    private final RestTemplate restTemplate;
    private final boolean enabled;
    private final String webhookUrl;

    public AlertWebhookNotifier(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${sentinel.notifications.webhook.enabled:false}") boolean enabled,
            @Value("${sentinel.notifications.webhook.url:}") String webhookUrl
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> requestFactory)
                .build();
        this.enabled = enabled;
        this.webhookUrl = webhookUrl;
    }

    @Async("alertNotificationExecutor")
    public void send(AlertWebhookPayload payload) {
        if (!enabled) {
            return;
        }
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Alert webhook notifications are enabled but ALERT_WEBHOOK_URL is empty");
            return;
        }

        try {
            EndpointUrlValidator.validatePublicHttpUrl(webhookUrl);
            restTemplate.postForEntity(webhookUrl, payload, Void.class);
            log.debug("Delivered alert webhook event {} for alert {}", payload.event(), payload.alertId());
        } catch (Exception exception) {
            // A notification outage must never interrupt endpoint monitoring or alert persistence.
            log.warn("Alert webhook delivery failed for alert {}: {}",
                    payload.alertId(), exception.getMessage());
        }
    }
}
