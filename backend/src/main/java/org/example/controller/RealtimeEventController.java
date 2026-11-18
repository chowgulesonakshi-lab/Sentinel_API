package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.realtime.RealtimeEventService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class RealtimeEventController {

    private final RealtimeEventService realtimeEventService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Authentication authentication) {
        return realtimeEventService.subscribe(authentication.getName());
    }
}
