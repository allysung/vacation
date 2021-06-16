package com.storm.vacation.annuals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annual Dto
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnualDto {

    private Long id;

    /**
     * 사용자 id
     */
    private Long accountId;

    /**
     * 총 연차 수
     */
    private double totalAnnualNum;

    /**
     * 총 사용 연차 수
     */
    private double totalAnnualNumOfUse;

    @Builder.Default
    private Set<AnnualDetailDto> annualDetails = new LinkedHashSet<>();

    public void setAnnualDetails(Set<AnnualDetailDto> annualDetails) {
        this.annualDetails = annualDetails.stream()
                .filter(annualDetailDto -> annualDetailDto.getAnnualStatus() != AnnualStatus.CANCEL)
                .sorted(Comparator.comparing(AnnualDetailDto::getId).reversed())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
