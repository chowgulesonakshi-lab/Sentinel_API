package org.example.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EndpointUrlValidatorTest {

    @Test
    void rejectsMissingAndMalformedUrls() {
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl(null));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl(" "));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("not-a-url"));
    }

    @Test
    void rejectsNonHttpSchemesAndEmbeddedCredentials() {
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("file:///etc/passwd"));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("https://user:password@example.com/health"));
    }

    @Test
    void rejectsLocalAndPrivateTargets() {
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("http://localhost:8080/health"));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("http://127.0.0.1:8080/health"));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("http://10.0.0.5/health"));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("http://192.0.2.10/health"));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("http://[::1]/health"));
    }

    @Test
    void rejectsPrivateHostnameSuffixes() {
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("http://service.internal/health"));
        assertThrows(IllegalArgumentException.class,
                () -> EndpointUrlValidator.validatePublicHttpUrl("http://printer.local/health"));
    }
}
