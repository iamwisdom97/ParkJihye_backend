package com.wire.wirebarley.integration;

import com.wire.wirebarley.domain.Account;
import com.wire.wirebarley.dto.*;
import com.wire.wirebarley.repository.AccountRepository;
import com.wire.wirebarley.service.AccountService;
import com.wire.wirebarley.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TransactionIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    private String accountNumber1;
    private String accountNumber2;

    @BeforeEach
    void setUp() {
        accountNumber1 = "1234567890";
        accountNumber2 = "0987654321";

        accountService.createAccount(new AccountCreateRequest(accountNumber1));
        accountService.createAccount(new AccountCreateRequest(accountNumber2));
    }

    @Test
    @DisplayName("입금 -> 출금 -> 이체 통합 테스트")
    void transactionFlow() {
        // 입금
        DepositRequest depositRequest = new DepositRequest(accountNumber1, new BigDecimal("500000"));
        TransactionResponse depositResponse = transactionService.deposit(depositRequest);
        assertThat(depositResponse.balanceAfter()).isEqualByComparingTo(new BigDecimal("500000"));

        // 출금
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest(accountNumber1, new BigDecimal("100000"));
        TransactionResponse withdrawalResponse = transactionService.withdraw(withdrawalRequest);
        assertThat(withdrawalResponse.balanceAfter()).isEqualByComparingTo(new BigDecimal("400000"));

        // 이체
        TransferRequest transferRequest = new TransferRequest(accountNumber1, accountNumber2, new BigDecimal("100000"));
        TransferResponse transferResponse = transactionService.transfer(transferRequest);

        assertThat(transferResponse.transferAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(transferResponse.fee()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(transferResponse.sourceBalanceAfter()).isEqualByComparingTo(new BigDecimal("299000.00"));
        assertThat(transferResponse.targetBalanceAfter()).isEqualByComparingTo(new BigDecimal("100000"));

        // 거래내역 조회
        List<TransactionResponse> history = transactionService.getTransactionHistory(accountNumber1);
        assertThat(history).hasSize(3); // deposit, withdrawal, transfer_out
    }

    @Test
    @DisplayName("계좌 잔액 검증 통합 테스트")
    void balanceVerification() {
        // 초기 잔액 확인
        Account account = accountRepository.findByAccountNumber(accountNumber1).orElseThrow();
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        // 입금 후 잔액 확인
        transactionService.deposit(new DepositRequest(accountNumber1, new BigDecimal("1000000")));
        account = accountRepository.findByAccountNumber(accountNumber1).orElseThrow();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000000"));

        // 이체 후 잔액 확인 (금액 + 수수료 차감)
        transactionService.transfer(new TransferRequest(accountNumber1, accountNumber2, new BigDecimal("100000")));
        account = accountRepository.findByAccountNumber(accountNumber1).orElseThrow();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("899000.00"));

        Account targetAccount = accountRepository.findByAccountNumber(accountNumber2).orElseThrow();
        assertThat(targetAccount.getBalance()).isEqualByComparingTo(new BigDecimal("100000"));
    }
}