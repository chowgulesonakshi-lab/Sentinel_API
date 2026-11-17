package org.example.serviceImplDb;
import com.fasterxml.jackson.databind.ObjectMapper; import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.example.entity.*; import org.example.repository.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.client.RestClient;
import java.time.*; import java.util.*; import java.util.stream.Collectors;
@Service @RequiredArgsConstructor @Slf4j
public class IncidentIntelligenceService {
 private final IncidentRepository incidents; private final MetricRepository metrics; private final IncidentEventRepository events; private final ObjectMapper mapper;
 @Value("${sentinel.ai.base-url:http://localhost:11434}") private String baseUrl;
 @Value("${sentinel.ai.model:qwen2.5:7b}") private String model;
 @Value("${sentinel.ai.enabled:true}") private boolean enabled;
 private String user(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null||!a.isAuthenticated()||"anonymousUser".equals(a.getName()))throw new AccessDeniedException("Authentication required");return a.getName().trim();}
 private Incident own(Long id){return incidents.findByIdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(id,user()).orElseThrow(()->new AccessDeniedException("Incident not found"));}
 private List<Metrics> history(Incident i){return metrics.findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndEndpoint_IdAndTimeStampBetweenOrderByTimeStampDesc(user(),i.getEndpoint().getId(),LocalDateTime.now().minusDays(30),LocalDateTime.now());}
 private String generate(String prompt){if(!enabled)return null;try{Map<?,?> r=RestClient.builder().baseUrl(baseUrl).build().post().uri("/api/generate").body(Map.of("model",model,"prompt",prompt,"stream",false)).retrieve().body(Map.class);Object value=r==null?null:r.get("response");return value==null?null:value.toString().trim();}catch(Exception ex){log.warn("AI provider unavailable: {}",ex.getMessage());return null;}}
 @Transactional(readOnly=true) public Map<String,Object> analyze(Long id){
  Incident i=own(id);List<Metrics> h=history(i);List<Incident> prior=incidents.findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCaseOrderByCreatedAtDesc(i.getEndpoint().getId(),user());
  long failed=h.stream().filter(m->!m.isSuccess()).count(),serverErrors=h.stream().filter(m->m.getStatusCode()!=null&&m.getStatusCode()>=500).count();
  double avg=h.stream().filter(m->m.getResponseTime()!=null).mapToDouble(Metrics::getResponseTime).average().orElse(0);
  List<String> evidence=new ArrayList<>(List.of("Incident created "+i.getCreatedAt()+"; status "+i.getStatus()+"; severity "+i.getSeverity(),"30-day telemetry contains "+h.size()+" checks, "+failed+" failed checks, and "+serverErrors+" HTTP 5xx responses.","Mean recorded response time: "+String.format(Locale.ROOT,"%.1f ms",avg)));
  h.stream().filter(m->!m.isSuccess()).limit(5).forEach(m->evidence.add("Failed check "+m.getTimeStamp()+": HTTP "+m.getStatusCode()+", "+m.getResponseTime()+" ms"));
  Map<String,Long> patterns=prior.stream().filter(p->!p.getId().equals(id)).collect(Collectors.groupingBy(p->p.getTitle().toLowerCase(Locale.ROOT),Collectors.counting()));
  List<String> recurring=patterns.entrySet().stream().filter(e->e.getValue()>0).map(e->e.getKey()+" occurred "+(e.getValue()+1)+" times on this endpoint").toList();
  String facts=String.join("\n",evidence)+"\nRecurring incidents: "+recurring;
  String generated=generate("You are an SRE. Analyze only the evidence. Clearly separate facts from hypotheses; never state an unproven cause as fact. Give root-cause hypotheses, investigation steps and corrective actions.\n"+i.getTitle()+"\n"+facts);
  String likely=serverErrors>0?"HTTP 5xx responses are present; inspect application and downstream dependency logs.":failed>0?"Failed probes exist; telemetry alone cannot distinguish network, TLS, DNS, or application causes.":"No failed checks in the last 30 days; the issue may be transient or outside this window.";
  Map<String,Object> out=new LinkedHashMap<>();out.put("incidentId",id);out.put("mode",generated==null?"EVIDENCE_BASED_FALLBACK":"AI_ANALYSIS");out.put("model",generated==null?"Sentinel telemetry rules":model);
  out.put("summary",generated==null?"Evidence-based analysis: "+facts:generated);out.put("likelyCause",likely);out.put("confidence",generated==null?45:65);
  out.put("confidenceExplanation","Confidence is limited by available telemetry; hypotheses require operator verification.");out.put("evidence",evidence);out.put("recurringPatterns",recurring);
  out.put("investigationSteps",List.of("Compare status codes and latency against the endpoint baseline.","Inspect application and dependency logs at the incident timestamp.","Check correlated failures in dependent services."));
  out.put("correctiveActions",List.of("Restore the endpoint or dependency and verify with successful probes.","Add regression tests for the observed failure mode.","Review timeout, retry, and alert thresholds after recovery."));
  out.put("affectedEndpoints",List.of(i.getEndpoint().getMethod()+" "+i.getEndpoint().getUrl()));out.put("generatedAt",LocalDateTime.now());return out;
 }
 @Transactional(readOnly=true) public Map<String,Object> postmortem(Long id){
  Incident i=own(id);Map<String,Object> a=analyze(id);List<String> timeline=events.findByIncident_IdOrderByCreatedAtAsc(id).stream().map(e->e.getCreatedAt()+" | "+e.getEventType()+" | "+e.getDetails()).toList();
  String prompt="Write a blameless SRE postmortem with impact, timeline, detection, response, evidence-based root cause (label unknowns), what went well, improvements, and action items. Do not invent facts. Incident "+i.getTitle()+". Analysis "+a.get("summary")+". Timeline "+timeline;
  String generated=generate(prompt);a.put("mode",generated==null?"POSTMORTEM_TEMPLATE":"AI_POSTMORTEM");
  if(generated==null)a.put("summary","POSTMORTEM\nImpact: "+i.getDescription()+"\nTimeline:\n"+String.join("\n",timeline)+"\nEvidence: "+a.get("evidence")+"\nRoot-cause hypothesis: "+a.get("likelyCause")+"\nActions: "+a.get("correctiveActions"));else a.put("summary",generated);
  return a;
 }
 @Transactional(readOnly=true) public String ask(String question){
  if(question==null||question.isBlank()||question.length()>1500)throw new IllegalArgumentException("Question must be 1–1500 characters");
  List<Incident> mine=incidents.findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseOrderByCreatedAtDesc(user());
  String context=mine.stream().limit(50).map(i->"#"+i.getId()+" "+i.getTitle()+" status="+i.getStatus()+" severity="+i.getSeverity()+" created="+i.getCreatedAt()+" endpoint="+i.getEndpoint().getUrl()).collect(Collectors.joining("\n"));
  String answer=generate("Answer using only these account-owned incident records. State when evidence is insufficient.\n"+context+"\nQuestion: "+question);
  return answer==null?"AI provider unavailable. Your incident record count is "+mine.size()+". Review /incidents for structured records.":answer;
 }
}