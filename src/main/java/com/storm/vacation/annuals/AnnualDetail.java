package com.storm.vacation.annuals;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.storm.vacation.accounts.Account;
import com.storm.vacation.audit.AuditingDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 연차 상세 Entity
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "annual_detail")
public class AnnualDetail extends AuditingDomain {

    @Id
    @Include
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annual_id", foreignKey = @ForeignKey(name = "fk_annual_id"))
    @JsonBackReference
    private Annual annual;

    @Enumerated(EnumType.STRING)
    private AnnualType annualType;

    /**
     * 연차수
     * 15일
     */
    @Builder.Default
    private double annualNum = 15;

    /**
     * 연차 사용수
     */
    @Builder.Default
    private double annualNumOfUse = 0;

    /**
     * 연차 요청 상태
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AnnualStatus annualStatus = AnnualStatus.REQUEST;

    /**
     * 코멘트
     */
    private String comment;

    /**
     * 시작일
     */
    private LocalDate startDate;

    /**
     * 종료일
     */
    private LocalDate endDate;

    public Account getAccount() {
        return Optional.ofNullable(annual)
                .map(Annual::getAccount)
                .orElse(null);
    }

    /**
     * 연차 사용 처리
     *
     * @param annualDetailDto
     * @return
     */
    public Double useAnnual(AnnualDetailDto annualDetailDto) {

        AnnualType reqAnnualType = annualDetailDto.getAnnualType();

        this.endDate = Optional.ofNullable(annualDetailDto.getEndDate())
                .orElse(annualDetailDto.getStartDate());

        double days = Optional.ofNullable(annualDetailDto.getRequestDays()).orElse(0d);

        double totalAnnualNum = annual.getTotalAnnualNum();

        final String message = "bad request";
        if (AnnualType.ANNUAL == reqAnnualType) {

            if (totalAnnualNum < days) {
                throw new IllegalArgumentException(message);
            }
            annualNumOfUse += days;
            this.annualNum = totalAnnualNum - days;

        } else if (AnnualType.HALF_ANNUAL == reqAnnualType) {

            double day = this.annualType.getDay();
            if (totalAnnualNum < day) {
                throw new IllegalArgumentException(message);
            }
            annualNumOfUse += day;
            this.annualNum = totalAnnualNum - day;

        } else if (AnnualType.QUARTER_ANNUAL == reqAnnualType) {
            double day = this.annualType.getDay();
            if (totalAnnualNum < day) {
                throw new IllegalArgumentException(message);
            }
            annualNumOfUse += day;
            this.annualNum = totalAnnualNum - day;
        }

        annual.setTotalAnnualNum(this.annualNum);
        annual.setTotalAnnualNumOfUse(annual.getTotalAnnualNumOfUse() + annualNumOfUse);
        return this.annualNumOfUse;
    }
}
