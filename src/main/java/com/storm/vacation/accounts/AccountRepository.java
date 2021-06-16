package com.storm.vacation.accounts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * email 로 사용자 조회
     *
     * @param email
     * @return
     */
    Optional<Account> findByEmail(String email);
}
