package org.example.controller;
import jakarta.validation.constraints.NotBlank; import lombok.Data; import lombok.RequiredArgsConstructor; import org.example.serviceImplDb.IncidentIntelligenceService;
import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/ai/incidents") @RequiredArgsConstructor
public class IncidentIntelligenceController {
 private final IncidentIntelligenceService service;
 @PostMapping("/{id}/analysis") public Map<String,Object> analyze(@PathVariable Long id){return service.analyze(id);}
 @PostMapping("/{id}/postmortem") public Map<String,Object> postmortem(@PathVariable Long id){return service.postmortem(id);}
 @PostMapping("/query") public Map<String,String> ask(@RequestBody Query body){if(body.getQuestion()==null||body.getQuestion().isBlank())throw new IllegalArgumentException("Question is required");return Map.of("answer",service.ask(body.getQuestion()));}
 @Data public static class Query{@NotBlank private String question;}
}