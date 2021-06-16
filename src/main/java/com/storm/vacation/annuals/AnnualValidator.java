package com.storm.vacation.annuals;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class AnnualValidator {

    public void validate(Annual annual, AnnualDetailDto annualDetailDto, Errors errors) {

        LocalDate startDate = annualDetailDto.getStartDate();
        LocalDate endDate = Optional.ofNullable(annualDetailDto.getEndDate())
                .orElseGet(() -> annualDetailDto.getStartDate().plusDays(1));


        double days = Optional.ofNullable(annualDetailDto.getRequestDays()).orElse(0d);

        if (annual.getTotalAnnualNum() < days) {
            errors.reject("wrongDay", "Exceeding the possible day.");
        }

        if (startDate.toEpochDay() > endDate.toEpochDay()) {
            errors.reject("wrongDay", "The start date cannot be greater than the end date.");
        }
    }
}
