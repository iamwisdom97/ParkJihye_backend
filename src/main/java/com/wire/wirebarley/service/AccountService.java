package com.wire.wirebarley.service;

import com.wire.wirebarley.domain.Account;
import com.wire.wirebarley.dto.AccountCreateRequest;
import com.wire.wirebarley.dto.AccountResponse;
import com.wire.wirebarley.exception.BusinessException;
import com.wire.wirebarley.exception.ErrorCode;
import com.wire.wirebarley.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }

        Account account = new Account(request.accountNumber());
        Account savedAccount = accountRepository.save(account);

        log.info("Account created: {}", savedAccount.getAccountNumber());
        return new AccountResponse(savedAccount);
    }

    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        accountRepository.delete(account);
        log.info("Account deleted: {}", accountNumber);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        return new AccountResponse(account);
    }

    @Transactional(readOnly = true)
    public Account findAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Transactional
    public Account findAccountByAccountNumberWithLock(String accountNumber) {
        return accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}