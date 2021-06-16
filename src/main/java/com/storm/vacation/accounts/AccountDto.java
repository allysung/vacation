package com.storm.vacation.accounts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 계정 Dto
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {

    private Long id;

    private String email;
}
