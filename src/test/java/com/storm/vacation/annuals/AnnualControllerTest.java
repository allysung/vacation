package com.storm.vacation.annuals;

import com.storm.vacation.accounts.Account;
import com.storm.vacation.accounts.AccountRepository;
import com.storm.vacation.accounts.AccountRole;
import com.storm.vacation.accounts.AccountService;
import com.storm.vacation.common.AppProperties;
import com.storm.vacation.common.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 휴가 요청 관련 Controller Test
 */

class AnnualControllerTest extends BaseTest {

    @Autowired
    AnnualRepository annualRepository;

    @Autowired
    AnnualDetailRepository annualDetailRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @BeforeEach
    void setUp() {
        this.annualRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    private Account createAccount() {
        Account account = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        return this.accountService.saveAccount(account);
    }

    private String getBearerToken(boolean needToCreateAccount) throws Exception {
        return "Bearer " + getAccessToken(needToCreateAccount);
    }

    private String getAccessToken(boolean needToCreateAccount) throws Exception {
        // Given
        if (needToCreateAccount) {
            createAccount();
        }

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));

        var responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    @Test
    @DisplayName("정상적으로 휴가 요청 생성하는 테스트")
    void createAnnual() throws Exception {
        AnnualDetailDto annualDetailDto = AnnualDetailDto.builder()
                .comment("여름휴가")
                .startDate(LocalDate.of(2020, 5, 3))
                .endDate(LocalDate.of(2020, 5, 6))
                .annualType(AnnualType.ANNUAL)
                .annualStatus(AnnualStatus.REQUEST)
                .requestDays(3d)
                .build();

        mockMvc.perform(post("/api/v1/annuals/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(annualDetailDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())

                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        ;
    }

    @Test
    @DisplayName("연차 요청 조회하기")
    void getAnnual() throws Exception {
        // Given
        Account account = this.createAccount();
        Annual annual = this.generateAnnual(1, account);

        // When & Then
        this.mockMvc.perform(get("/api/v1/annuals/{id}", annual.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @DisplayName("자신의 연차 요청 조회하기")
    void getAnnualMe() throws Exception {
        // Given
        Account account = this.createAccount();

        // When & Then
        this.mockMvc.perform(get("/api/v1/annuals/me")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @DisplayName("자신의 요청 조회하기 - 토큰이 없는 경우")
    void getAnnualMe_without_token() throws Exception {

        // When & Then
        this.mockMvc.perform(get("/api/v1/annuals/me"))
                .andExpect(status().isUnauthorized())
        ;
    }

    private Annual generateAnnual(int index, Account account) {
        Annual annual = buildAnnual(index);
        annual.setAccount(account);
        return this.annualRepository.save(annual);
    }

    private Annual buildAnnual(int index) {
        Annual annual = Annual.builder()
                .totalAnnualNum(15)
                .build();
        AnnualDetail annualDetail = AnnualDetail.builder()
                .startDate(LocalDate.now())
                .comment("annual " + index).build();
        annual.addAnnualDetail(annualDetail);
        return annual;
    }
}