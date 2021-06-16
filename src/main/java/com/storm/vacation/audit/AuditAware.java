package com.storm.vacation.audit;

import java.util.Optional;

import com.storm.vacation.accounts.AccountAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * this class for Audit 을 위하여 사용자 정보를 주입한다.
 */
@Slf4j
public class AuditAware implements AuditorAware<Long> {

  @Override
  public Optional<Long> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (null == authentication || authentication.isAuthenticated() == false
        || StringUtils.equals("anonymousUser", String.valueOf(authentication.getPrincipal()))) {
      return Optional.of(-1L);
    }

    log.debug("authentication : {}, {}", authentication.getPrincipal(), authentication);

    try {
      AccountAdapter accountAdapter = (AccountAdapter) authentication.getPrincipal();
      return Optional.ofNullable(accountAdapter.getAccount().getId());
    } catch (Exception e) {
      log.error("ERROR : UserDetail failed!", e);
    }

    return Optional.of(-1L);
  }
}
