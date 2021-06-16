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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 휴가 요청 관련 Controller Test
 */
class AnnualDetailControllerTest extends BaseTest {

    @Autowired
    AnnualRepository annualRepository;

    @Autowired
    AnnualDetailRepository annualDetailRepository;

    @Autowired
    AnnualService annualService;

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


    private Annual generateAnnual(int index) {
        Annual annual = buildAnnual(index);
        return this.annualRepository.save(annual);
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

    @Test
    @DisplayName("연차 상세 조회하기")
    void getAnnualDetail() throws Exception {
        // Given
        Account account = this.createAccount();
        Annual annual = this.generateAnnual(2, account);
        AnnualDetail annualDetail = annual.getAnnualDetails().stream().findFirst().get();

        // When & Then
        this.mockMvc.perform(get("/api/v1/annuals/details/{id}", annualDetail.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @DisplayName("연차 요청 취소하기")
    void cancelAnnualDetail() throws Exception {
        // given
        Account account = this.createAccount();
        Annual annual = this.generateAnnual(3, account);

        AnnualDetail annualDetail = annual.getAnnualDetails().stream().findFirst().get();
        AnnualDetailDto annualDetailDto = modelMapper.map(annualDetail, AnnualDetailDto.class);
        annualDetailDto.setAnnualStatus(AnnualStatus.CANCEL);

        // When & Then
        this.mockMvc.perform(patch("/api/v1/annuals/details/{id}", annualDetail.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(annualDetailDto)))
                .andDo(print())
                .andExpect(status().isOk())
                // 취소 휴가 요청은 목록에서 제외
                .andExpect(jsonPath("annualDetails").isEmpty())
                .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @DisplayName("연차 요청 취소 - 이미 시작시 에러 발생")
    void cancelAnnualDetail_Bad_Request() throws Exception {
        // given
        Account account = this.createAccount();
        Annual annual = this.generateAnnual(3, account);

        AnnualDetail annualDetail = annual.getAnnualDetails().stream().findFirst().get();
        annualDetail.setStartDate(LocalDate.now().minusDays(20));
        annual.addAnnualDetail(annualDetail);
        annualRepository.save(annual);

        AnnualDetailDto annualDetailDto = modelMapper.map(annualDetail, AnnualDetailDto.class);
        annualDetailDto.setAnnualStatus(AnnualStatus.CANCEL);

        // When & Then
        this.mockMvc.perform(patch("/api/v1/annuals/details/{id}", annualDetail.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(annualDetailDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @DisplayName("연차 목록 30개를 2페이지 조회 하기")
    void queryAnnualDetails() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generateAnnual);

        // When & Then
        this.mockMvc.perform(get("/api/v1/annuals/details")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.annualDetails[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @DisplayName("연차 목록 30개를 2페이지 조회 하기")
    void queryAnnualDetailsWithAuthentication() throws Exception {

        // Given
        IntStream.range(0, 30).forEach(this::generateAnnual);

        // When & Then
        this.mockMvc.perform(get("/api/v1/annuals/details")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.annualDetails[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-annuals").exists())
        ;
    }
}