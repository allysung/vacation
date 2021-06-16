package com.storm.vacation.annuals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 연차 타입
 * <p>
 * 연차(1일) /반차(0.5일) /반반차 (0.25일)
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AnnualType {

    ANNUAL("연차", 1),
    HALF_ANNUAL("반차", 0.5),
    QUARTER_ANNUAL("반반차", 0.25);

    private String title;
    private double day;
}
