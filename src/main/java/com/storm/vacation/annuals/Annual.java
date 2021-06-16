package com.storm.vacation.annuals;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.storm.vacation.accounts.AccountSerializer;
import com.storm.vacation.accounts.Account;
import com.storm.vacation.audit.AuditingDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 연차 Entity
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "annual")
public class Annual extends AuditingDomain {

    @Id
    @GeneratedValue
    @Include
    private Long id;

    @JsonSerialize(using = AccountSerializer.class)
    @OneToOne(fetch = FetchType.LAZY)
    private Account account;

    /**
     * 연차수 15일의 연차(1년에 15개)
     */
    @Default
    private double totalAnnualNum = 15;

    @Default
    private double totalAnnualNumOfUse = 0;

    @Default
    @OneToMany(mappedBy = "annual", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<AnnualDetail> annualDetails = new LinkedHashSet<>();

    public void addAnnualDetail(AnnualDetail annualDetail) {
        this.getAnnualDetails().add(annualDetail);
        annualDetail.setAnnual(this);
    }

    public Long getAccountId() {
        return Optional.ofNullable(account)
                .map(Account::getId)
                .orElse(null);
    }
}
