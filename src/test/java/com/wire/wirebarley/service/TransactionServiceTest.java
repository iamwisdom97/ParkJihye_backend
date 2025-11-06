package com.wire.wirebarley.service;

import com.wire.wirebarley.domain.Account;
import com.wire.wirebarley.domain.DailyLimit;
import com.wire.wirebarley.domain.Transaction;
import com.wire.wirebarley.dto.*;
import com.wire.wirebarley.exception.BusinessException;
import com.wire.wirebarley.exception.ErrorCode;
import com.wire.wirebarley.repository.DailyLimitRepository;
import com.wire.wirebarley.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private DailyLimitRepository dailyLimitRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("입금 성공")
    void deposit_Success() {
        // given
        String accountNumber = "1234567890";
        BigDecimal amount = new BigDecimal("10000");
        Account account = new Account(accountNumber);
        DepositRequest request = new DepositRequest(accountNumber, amount);

        given(accountService.findAccountByAccountNumberWithLock(accountNumber)).willReturn(account);
        given(transactionRepository.save(any(Transaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        TransactionResponse response = transactionService.deposit(request);

        // then
        assertThat(response.accountNumber()).isEqualTo(accountNumber);
        assertThat(response.amount()).isEqualByComparingTo(amount);
        verify(accountService).findAccountByAccountNumberWithLock(accountNumber);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("출금 성공")
    void withdraw_Success() {
        // given
        String accountNumber = "1234567890";
        BigDecimal initialBalance = new BigDecimal("100000");
        BigDecimal withdrawAmount = new BigDecimal("50000");

        Account account = new Account(accountNumber);
        account.deposit(initialBalance);

        WithdrawalRequest request = new WithdrawalRequest(accountNumber, withdrawAmount);
        DailyLimit dailyLimit = new DailyLimit(accountNumber, LocalDate.now());

        given(accountService.findAccountByAccountNumberWithLock(accountNumber)).willReturn(account);
        given(dailyLimitRepository.findByAccountNumberAndTransactionDateWithLock(accountNumber, LocalDate.now()))
                .willReturn(Optional.of(dailyLimit));
        given(dailyLimitRepository.save(any(DailyLimit.class))).willReturn(dailyLimit);
        given(transactionRepository.save(any(Transaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        TransactionResponse response = transactionService.withdraw(request);

        // then
        assertThat(response.amount()).isEqualByComparingTo(withdrawAmount);
        verify(accountService).findAccountByAccountNumberWithLock(accountNumber);
        verify(dailyLimitRepository).save(any(DailyLimit.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("출금 실패 - 일일 한도 초과")
    void withdraw_DailyLimitExceeded() {
        // given
        String accountNumber = "1234567890";
        BigDecimal initialBalance = new BigDecimal("2000000");
        BigDecimal withdrawAmount = new BigDecimal("600000");

        Account account = new Account(accountNumber);
        account.deposit(initialBalance);

        WithdrawalRequest request = new WithdrawalRequest(accountNumber, withdrawAmount);
        DailyLimit dailyLimit = new DailyLimit(accountNumber, LocalDate.now());
        dailyLimit.addWithdrawalAmount(new BigDecimal("500000"));

        given(accountService.findAccountByAccountNumberWithLock(accountNumber)).willReturn(account);
        given(dailyLimitRepository.findByAccountNumberAndTransactionDateWithLock(accountNumber, LocalDate.now()))
                .willReturn(Optional.of(dailyLimit));

        // when & then
        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("이체 성공")
    void transfer_Success() {
        // given
        String sourceAccountNumber = "1234567890";
        String targetAccountNumber = "0987654321";
        BigDecimal transferAmount = new BigDecimal("100000");

        Account sourceAccount = new Account(sourceAccountNumber);
        sourceAccount.deposit(new BigDecimal("500000"));

        Account targetAccount = new Account(targetAccountNumber);

        TransferRequest request = new TransferRequest(sourceAccountNumber, targetAccountNumber, transferAmount);
        DailyLimit dailyLimit = new DailyLimit(sourceAccountNumber, LocalDate.now());

        given(accountService.findAccountByAccountNumberWithLock(sourceAccountNumber)).willReturn(sourceAccount);
        given(accountService.findAccountByAccountNumberWithLock(targetAccountNumber)).willReturn(targetAccount);
        given(accountService.findAccountByAccountNumber(sourceAccountNumber)).willReturn(sourceAccount);
        given(accountService.findAccountByAccountNumber(targetAccountNumber)).willReturn(targetAccount);
        given(dailyLimitRepository.findByAccountNumberAndTransactionDateWithLock(sourceAccountNumber, LocalDate.now()))
                .willReturn(Optional.of(dailyLimit));
        given(dailyLimitRepository.save(any(DailyLimit.class))).willReturn(dailyLimit);
        given(transactionRepository.save(any(Transaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        TransferResponse response = transactionService.transfer(request);

        // then
        assertThat(response.transferAmount()).isEqualByComparingTo(transferAmount);
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("1000.00"));
        verify(transactionRepository, times(2)).save(any(Transaction.class)); // transfer_out and transfer_in
    }

    @Test
    @DisplayName("이체 실패 - 동일 계좌")
    void transfer_SameAccount() {
        // given
        String accountNumber = "1234567890";
        BigDecimal transferAmount = new BigDecimal("100000");
        TransferRequest request = new TransferRequest(accountNumber, accountNumber, transferAmount);

        // when & then
        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAME_ACCOUNT_TRANSFER);
    }
}