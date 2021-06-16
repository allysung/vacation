package com.storm.vacation.annuals;

import com.storm.vacation.accounts.Account;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 휴가 신청 Service 로직 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnualServiceImpl implements AnnualService {

    private final AnnualRepository annualRepository;

    private final AnnualDetailRepository annualDetailRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Annual findOrCreateNew(Account account) {

        return annualRepository.findAnnualByAccount(account)
                .orElseGet(() -> annualRepository.save(Annual.builder()
                        .account(account)
                        .build())
                );
    }

    @Override
    @Transactional
    public Annual requestVacation(Annual annual, AnnualDetailDto requestAnnualDto) {

        AnnualDetail annualDetail = modelMapper.map(requestAnnualDto, AnnualDetail.class);
        annualDetail.setAnnual(annual);

        annualDetail.useAnnual(requestAnnualDto);

        annual.addAnnualDetail(annualDetail);

        annualRepository.save(annual);
        return annual;
    }

    @Override
    @Transactional
    public Annual cancel(AnnualDetail annualDetail) {

        Annual annual = annualDetail.getAnnual();
        double totalAnnualNum = annual.getTotalAnnualNum();
        double totalNumOfUse = annual.getTotalAnnualNumOfUse();

        annualDetail.setAnnualStatus(AnnualStatus.CANCEL);

        double numOfUse = annualDetail.getAnnualNumOfUse();

        annual.setTotalAnnualNum(totalAnnualNum + numOfUse);
        annual.setTotalAnnualNumOfUse(totalNumOfUse - numOfUse);

        annual.addAnnualDetail(annualDetail);
        annualRepository.save(annual);

        return annual;
    }

    @Override
    public Optional<Annual> getAnnual(Long id) {
        return annualRepository.findById(id);
    }

    @Override
    public Optional<AnnualDetail> getAnnualDetail(Long id) {
        return annualDetailRepository.findById(id);
    }

    @Override
    public Page<AnnualDetail> getAnnualDetails(Pageable pageable) {
        return annualDetailRepository.findAll(pageable);
    }
}
