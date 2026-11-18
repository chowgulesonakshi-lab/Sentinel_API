package org.example.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void generatesCorrelationIdWhenRequestDoesNotProvideOne() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        String correlationId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertNotNull(correlationId);
        assertTrue(correlationId.matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        ));
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void preservesValidIncomingCorrelationIdAndAddsItToMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "client-request_123");

        String[] observedId = new String[1];
        FilterChain chain = (servletRequest, servletResponse) ->
                observedId[0] = MDC.get("correlationId");

        filter.doFilter(request, response, chain);

        assertEquals("client-request_123",
                response.getHeader(CorrelationIdFilter.HEADER_NAME));
        assertEquals("client-request_123", observedId[0]);
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void replacesInvalidIncomingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "invalid id\r\nInjected-Header: yes");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        String correlationId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertNotNull(correlationId);
        assertFalse(correlationId.contains("\r"));
        assertFalse(correlationId.contains("\n"));
        assertNotEquals("invalid id\r\nInjected-Header: yes", correlationId);
    }
}
