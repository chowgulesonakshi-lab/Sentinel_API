package org.example.serviceImplDb;
import lombok.RequiredArgsConstructor; import org.example.entity.*; import org.example.repository.*; import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.*; import java.util.*;
@Service @RequiredArgsConstructor
public class IncidentService {
 private final IncidentRepository incidents; private final IncidentEventRepository events; private final EndpointRepository endpoints; private final AlertsRepository alerts;
 private String user(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null||!a.isAuthenticated()||"anonymousUser".equals(a.getName()))throw new AccessDeniedException("Authentication required");return a.getName().trim();}
 private Incident own(Long id){return incidents.findByIdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(id,user()).orElseThrow(()->new AccessDeniedException("Incident not found"));}
 private void event(Incident i,String type,String details){IncidentEvent e=new IncidentEvent();e.setIncident(i);e.setActorEmail(user());e.setEventType(type);e.setDetails(details);events.save(e);}
 private Map<String,Object> view(Incident i,boolean timeline){
  Map<String,Object> m=new LinkedHashMap<>();m.put("id",i.getId());m.put("endpointId",i.getEndpoint().getId());m.put("endpointUrl",i.getEndpoint().getUrl());
  m.put("sourceAlertId",i.getSourceAlert()==null?null:i.getSourceAlert().getId());m.put("title",i.getTitle());m.put("description",i.getDescription());
  m.put("status",i.getStatus());m.put("severity",i.getSeverity());m.put("priority",i.getPriority());m.put("assignedTo",i.getAssignedTo());m.put("createdBy",i.getCreatedBy());
  m.put("createdAt",i.getCreatedAt());m.put("acknowledgedAt",i.getAcknowledgedAt());m.put("investigatingAt",i.getInvestigatingAt());m.put("resolvedAt",i.getResolvedAt());
  m.put("mttrMinutes",i.getResolvedAt()==null?null:Duration.between(i.getCreatedAt(),i.getResolvedAt()).toMinutes());
  if(timeline)m.put("timeline",events.findByIncident_IdOrderByCreatedAtAsc(i.getId()).stream().map(e->Map.of("id",e.getId(),"actorEmail",e.getActorEmail(),"eventType",e.getEventType(),"details",e.getDetails()==null?"":e.getDetails(),"createdAt",e.getCreatedAt())).toList());
  return m;
 }
 @Transactional public Map<String,Object> create(Map<String,Object> body){
  Long endpointId=Long.valueOf(body.get("endpointId").toString()); Endpoint ep=endpoints.findByIdAndApiService_Project_Owner_EmailIgnoreCase(endpointId,user()).orElseThrow(()->new AccessDeniedException("Endpoint not found"));
  Incident i=new Incident();i.setEndpoint(ep);i.setTitle(Objects.toString(body.get("title"),"").trim());if(i.getTitle().isBlank())throw new IllegalArgumentException("Title is required");
  i.setDescription(Objects.toString(body.get("description"),null));i.setCreatedBy(user());i.setAssignedTo(Objects.toString(body.get("assignedTo"),null));
  i.setSeverity(enumValue(Incident.Severity.class,body.get("severity"),Incident.Severity.MEDIUM));i.setPriority(enumValue(Incident.Priority.class,body.get("priority"),Incident.Priority.P2));
  if(body.get("alertId")!=null){Alerts a=alerts.findById(Long.valueOf(body.get("alertId").toString())).orElseThrow(()->new IllegalArgumentException("Alert not found"));org.example.security.ResourceOwnership.requireOwner(a);if(a.getEndpoint()==null||!a.getEndpoint().getId().equals(ep.getId()))throw new IllegalArgumentException("Alert does not belong to endpoint");i.setSourceAlert(a);}
  incidents.save(i);event(i,"CREATED",i.getTitle());return view(i,true);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> list(Long endpointId){List<Incident> rows=endpointId==null?incidents.findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseOrderByCreatedAtDesc(user()):incidents.findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCaseOrderByCreatedAtDesc(endpointId,user());return rows.stream().map(i->view(i,false)).toList();}
 @Transactional(readOnly=true) public Map<String,Object> get(Long id){return view(own(id),true);}
 @Transactional public Map<String,Object> update(Long id,Map<String,Object> body){
  Incident i=own(id);Incident.Status next=enumValue(Incident.Status.class,body.get("status"),i.getStatus()),old=i.getStatus();
  boolean valid=old==next||(old==Incident.Status.OPEN&&next==Incident.Status.ACKNOWLEDGED)||(old==Incident.Status.ACKNOWLEDGED&&(next==Incident.Status.INVESTIGATING||next==Incident.Status.RESOLVED))||(old==Incident.Status.INVESTIGATING&&(next==Incident.Status.ACKNOWLEDGED||next==Incident.Status.RESOLVED));
  if(!valid)throw new IllegalArgumentException("Invalid transition "+old+" -> "+next);
  i.setStatus(next);LocalDateTime now=LocalDateTime.now();if(next==Incident.Status.ACKNOWLEDGED&&i.getAcknowledgedAt()==null)i.setAcknowledgedAt(now);if(next==Incident.Status.INVESTIGATING&&i.getInvestigatingAt()==null)i.setInvestigatingAt(now);if(next==Incident.Status.RESOLVED)i.setResolvedAt(now);
  if(body.containsKey("severity"))i.setSeverity(enumValue(Incident.Severity.class,body.get("severity"),i.getSeverity()));if(body.containsKey("priority"))i.setPriority(enumValue(Incident.Priority.class,body.get("priority"),i.getPriority()));if(body.containsKey("assignedTo"))i.setAssignedTo(Objects.toString(body.get("assignedTo"),null));
  incidents.save(i);event(i,"UPDATED","Status "+old+" -> "+next+"; "+Objects.toString(body.get("details"),""));return view(i,true);
 }
 @Transactional public Map<String,Object> note(Long id,String note){if(note==null||note.isBlank())throw new IllegalArgumentException("Note is required");Incident i=own(id);event(i,"NOTE",note.trim());return view(i,true);}
 @Transactional(readOnly=true) public Map<String,Object> summary(){List<Incident> all=incidents.findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseOrderByCreatedAtDesc(user());long resolved=all.stream().filter(i->i.getStatus()==Incident.Status.RESOLVED).count();double mttr=all.stream().filter(i->i.getResolvedAt()!=null).mapToLong(i->Duration.between(i.getCreatedAt(),i.getResolvedAt()).toMinutes()).average().orElse(0);return Map.of("total",all.size(),"open",all.size()-resolved,"resolved",resolved,"averageMttrMinutes",Math.round(mttr*100.0)/100.0);}
 private <E extends Enum<E>> E enumValue(Class<E> c,Object value,E fallback){if(value==null)return fallback;return Enum.valueOf(c,value.toString().toUpperCase(Locale.ROOT));}
}