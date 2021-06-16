package com.storm.vacation.annuals;


import com.storm.vacation.accounts.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 휴가 신청 Service
 */
public interface AnnualService {

    /**
     * 해당 사용자의 연차 조회(없을 경우 생성)
     *
     * @param account
     * @return
     */
    Annual findOrCreateNew(Account account);

    /**
     * 휴가 신청
     *
     * @param annual
     * @param requestAnnualDto
     * @return
     */
    Annual requestVacation(Annual annual, AnnualDetailDto requestAnnualDto);

    /**
     * 연차 취소
     *
     * @param annualDetail
     * @return
     */
    Annual cancel(AnnualDetail annualDetail);

    /**
     * 연차 조회
     *
     * @param id
     * @return
     */
    Optional<Annual> getAnnual(Long id);

    /**
     * 연차 상세 조회
     *
     * @param id
     * @return
     */
    Optional<AnnualDetail> getAnnualDetail(Long id);

    /**
     * 연차 상세 목록 조회
     *
     * @param pageable
     * @return
     */
    Page<AnnualDetail> getAnnualDetails(Pageable pageable);
}
