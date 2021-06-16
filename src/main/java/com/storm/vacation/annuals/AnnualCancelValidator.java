package com.storm.vacation.annuals;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDate;

@Component
public class AnnualCancelValidator {

    public void validate(AnnualDetail annualDetail, AnnualDetailDto annualDetailDto, Errors errors) {

        LocalDate startDate = annualDetail.getStartDate();

        if (LocalDate.now().isAfter(startDate)) {
            errors.reject("wrongDay", "Already begun cannot be canceled.");
        }

        if (annualDetailDto.getAnnualStatus() != AnnualStatus.CANCEL) {
            errors.rejectValue("annualStatus", "notSupport", "This request is not supported.");
        }

        if (annualDetail.getAnnualStatus() == AnnualStatus.CANCEL) {
            errors.reject("wrongValue", "This is a request that has already been canceled.");
        }
    }
}
