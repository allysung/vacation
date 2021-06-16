package com.storm.vacation.annuals;

import com.storm.vacation.accounts.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnnualRepository extends JpaRepository<Annual, Long> {

    Optional<Annual> findAnnualByAccount(Account account);
}
