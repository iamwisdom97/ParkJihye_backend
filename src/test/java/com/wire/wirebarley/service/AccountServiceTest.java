package com.wire.wirebarley.service;

import com.wire.wirebarley.domain.Account;
import com.wire.wirebarley.dto.AccountCreateRequest;
import com.wire.wirebarley.dto.AccountResponse;
import com.wire.wirebarley.exception.BusinessException;
import com.wire.wirebarley.exception.ErrorCode;
import com.wire.wirebarley.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccount_Success() {
        // given
        String accountNumber = "1234567890";
        AccountCreateRequest request = new AccountCreateRequest(accountNumber);
        Account account = new Account(accountNumber);

        given(accountRepository.existsByAccountNumber(accountNumber)).willReturn(false);
        given(accountRepository.save(any(Account.class))).willReturn(account);

        // when
        AccountResponse response = accountService.createAccount(request);

        // then
        assertThat(response.accountNumber()).isEqualTo(accountNumber);
        verify(accountRepository).existsByAccountNumber(accountNumber);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 이미 존재하는 계좌번호")
    void createAccount_AlreadyExists() {
        // given
        String accountNumber = "1234567890";
        AccountCreateRequest request = new AccountCreateRequest(accountNumber);

        given(accountRepository.existsByAccountNumber(accountNumber)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_ALREADY_EXISTS);

        verify(accountRepository).existsByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 삭제 성공")
    void deleteAccount_Success() {
        // given
        String accountNumber = "1234567890";
        Account account = new Account(accountNumber);

        given(accountRepository.findByAccountNumber(accountNumber)).willReturn(Optional.of(account));
        doNothing().when(accountRepository).delete(account);

        // when
        accountService.deleteAccount(accountNumber);

        // then
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).delete(account);
    }

    @Test
    @DisplayName("계좌 삭제 실패 - 존재하지 않는 계좌")
    void deleteAccount_NotFound() {
        // given
        String accountNumber = "1234567890";

        given(accountRepository.findByAccountNumber(accountNumber)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.deleteAccount(accountNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("계좌 조회 성공")
    void getAccount_Success() {
        // given
        String accountNumber = "1234567890";
        Account account = new Account(accountNumber);

        given(accountRepository.findByAccountNumber(accountNumber)).willReturn(Optional.of(account));

        // when
        AccountResponse response = accountService.getAccount(accountNumber);

        // then
        assertThat(response.accountNumber()).isEqualTo(accountNumber);
        verify(accountRepository).findByAccountNumber(accountNumber);
    }
}