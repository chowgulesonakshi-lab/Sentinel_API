package org.example.serviceImplDb;
import org.example.entity.*; import org.example.repository.*; import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {
 @Mock IncidentRepository incidents; @Mock IncidentEventRepository events; @Mock EndpointRepository endpoints; @Mock AlertsRepository alerts;
 IncidentService service; Endpoint endpoint;
 @BeforeEach void setup(){
  service=new IncidentService(incidents,events,endpoints,alerts);
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("owner@example.com","token",List.of()));
  endpoint=new Endpoint();endpoint.setId(8L);endpoint.setUrl("https://service.example/health");endpoint.setMethod(Endpoint.HttpMethod.GET);
  when(endpoints.findByIdAndApiService_Project_Owner_EmailIgnoreCase(8L,"owner@example.com")).thenReturn(Optional.of(endpoint));
  when(incidents.save(any(Incident.class))).thenAnswer(call->{Incident i=call.getArgument(0);if(i.getId()==null)i.setId(21L);if(i.getCreatedAt()==null)i.setCreatedAt(java.time.LocalDateTime.now());if(i.getUpdatedAt()==null)i.setUpdatedAt(java.time.LocalDateTime.now());return i;});
  when(events.save(any(IncidentEvent.class))).thenAnswer(call->call.getArgument(0));
  when(events.findByIncident_IdOrderByCreatedAtAsc(21L)).thenReturn(List.of());
 }
 @AfterEach void cleanup(){SecurityContextHolder.clearContext();}
 @Test void createsOwnerScopedIncidentWithInitialAuditEvent(){
  Map<String,Object> body=new HashMap<>();body.put("endpointId",8);body.put("title","API unavailable");body.put("severity","HIGH");body.put("priority","P1");
  Map<String,Object> result=service.create(body);
  assertEquals(21L,result.get("id"));assertEquals("OPEN",result.get("status").toString());assertEquals("HIGH",result.get("severity").toString());
  verify(events).save(argThat(e->"CREATED".equals(e.getEventType())));
 }
 @Test void enforcesLifecycleAndRecordsStatusChanges(){
  Incident i=new Incident();i.setId(21L);i.setEndpoint(endpoint);i.setTitle("API down");i.setCreatedBy("owner@example.com");i.setStatus(Incident.Status.OPEN);
  when(incidents.findByIdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(21L,"owner@example.com")).thenReturn(Optional.of(i));
  assertThrows(IllegalArgumentException.class,()->service.update(21L,Map.of("status","RESOLVED")));
  Map<String,Object> updated=service.update(21L,Map.of("status","ACKNOWLEDGED","assignedTo","on-call@example.com"));
  assertEquals("ACKNOWLEDGED",updated.get("status").toString());assertNotNull(updated.get("acknowledgedAt"));
  verify(events).save(argThat(e->"UPDATED".equals(e.getEventType())));
 }
}