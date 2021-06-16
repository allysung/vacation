package com.storm.vacation.config;

import com.storm.vacation.accounts.Account;
import com.storm.vacation.accounts.AccountRole;
import com.storm.vacation.accounts.AccountService;
import com.storm.vacation.common.AppProperties;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setPropertyCondition(Conditions.isNotNull())
                .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE)
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
        return modelMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Autowired
            AppProperties appProperties;

            @Override
            public void run(ApplicationArguments args) {
                Optional<Account> adminAccount = accountService.getAccount(appProperties.getAdminUsername());
                if (adminAccount.isEmpty()) {
                    Account admin = Account.builder()
                            .email(appProperties.getAdminUsername())
                            .password(appProperties.getAdminPassword())
                            .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                            .build();
                    accountService.saveAccount(admin);
                }

                Optional<Account> userAccount = accountService.getAccount(appProperties.getUserUsername());
                if (userAccount.isEmpty()) {
                    Account user = Account.builder()
                            .email(appProperties.getUserUsername())
                            .password(appProperties.getUserPassword())
                            .roles(Set.of(AccountRole.USER))
                            .build();
                    accountService.saveAccount(user);
                }
            }
        };
    }
}
