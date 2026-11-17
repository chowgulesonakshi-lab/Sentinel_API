package org.example.controller;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import lombok.Data; import lombok.RequiredArgsConstructor;
import org.example.serviceImplDb.IncidentService; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/incidents") @RequiredArgsConstructor @CrossOrigin(origins="*")
public class IncidentController {
 private final IncidentService service;
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> create(@RequestBody Map<String,Object> body){return service.create(body);}
 @GetMapping public List<Map<String,Object>> list(@RequestParam(required=false) Long endpointId){return service.list(endpointId);}
 @GetMapping("/summary") public Map<String,Object> summary(){return service.summary();}
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable Long id){return service.get(id);}
 @PatchMapping("/{id}") public Map<String,Object> update(@PathVariable Long id,@RequestBody Map<String,Object> body){return service.update(id,body);}
 @PostMapping("/{id}/notes") public Map<String,Object> note(@PathVariable Long id,@Valid @RequestBody NoteRequest body){return service.note(id,body.getNote());}
 @Data public static class NoteRequest{@NotBlank private String note;}
}