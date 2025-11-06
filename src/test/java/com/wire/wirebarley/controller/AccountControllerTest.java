package com.wire.wirebarley.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.wirebarley.dto.AccountCreateRequest;
import com.wire.wirebarley.dto.AccountResponse;
import com.wire.wirebarley.exception.BusinessException;
import com.wire.wirebarley.exception.ErrorCode;
import com.wire.wirebarley.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 API 성공")
    void createAccount_Success() throws Exception {
        // given
        String accountNumber = "1234567890";
        AccountCreateRequest request = new AccountCreateRequest(accountNumber);
        AccountResponse response = createAccountResponse(accountNumber);

        given(accountService.createAccount(any(AccountCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("계좌 생성 API 실패 - 유효성 검증 실패")
    void createAccount_ValidationFail() throws Exception {
        // given
        AccountCreateRequest request = new AccountCreateRequest("123"); // Invalid account number

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("계좌 삭제 API 성공")
    void deleteAccount_Success() throws Exception {
        // given
        String accountNumber = "1234567890";

        // when & then
        mockMvc.perform(delete("/api/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("계좌 삭제 API 실패 - 존재하지 않는 계좌")
    void deleteAccount_NotFound() throws Exception {
        // given
        String accountNumber = "1234567890";
        doThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND))
                .when(accountService).deleteAccount(accountNumber);

        // when & then
        mockMvc.perform(delete("/api/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("계좌 조회 API 성공")
    void getAccount_Success() throws Exception {
        // given
        String accountNumber = "1234567890";
        AccountResponse response = createAccountResponse(accountNumber);

        given(accountService.getAccount(accountNumber)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber));
    }

    private AccountResponse createAccountResponse(String accountNumber) {
        return new AccountResponse(new com.wire.wirebarley.domain.Account(accountNumber));
    }
}