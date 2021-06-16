package com.storm.vacation.annuals;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.storm.vacation.accounts.AccountDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;

/**
 * AnnualDetail dto
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Relation(collectionRelation = "annualDetails")
public class AnnualDetailDto {

    interface CancelVal {
    }

    private Long id;

    /**
     * 요청일
     */
    @Min(1)
    private Double requestDays;

    @NotNull
    private AnnualType annualType;

    @NotNull(groups = CancelVal.class)
    private AnnualStatus annualStatus;

    private String comment;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * 연차수
     */
    @Builder.Default
    private double annualNumOfUse = 0d;

    /**
     * 남은 연차수
     */
    @Builder.Default
    private double annualNum = 0d;

    @JsonIgnore
    private AccountDto account;

    @JsonProperty("accountId")
    public Long getAccountId() {
        return Optional.ofNullable(account)
                .map(AccountDto::getId)
                .orElse(null);
    }
}
