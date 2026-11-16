package org.example.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DependencyImpactResponseDTO {
    Long rootServiceId;
    List<Long> directDependentServiceIds;
    List<Long> indirectDependentServiceIds;
    List<Long> confirmedUnhealthyServiceIds;
    List<Long> potentiallyAffectedServiceIds;
}
