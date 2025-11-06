package com.wire.wirebarley.service;

import com.wire.wirebarley.domain.Account;
import com.wire.wirebarley.domain.DailyLimit;
import com.wire.wirebarley.domain.Transaction;
import com.wire.wirebarley.dto.*;
import com.wire.wirebarley.exception.BusinessException;
import com.wire.wirebarley.exception.ErrorCode;
import com.wire.wirebarley.repository.DailyLimitRepository;
import com.wire.wirebarley.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("1000000");
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("3000000");
    private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.01");

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final DailyLimitRepository dailyLimitRepository;

    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountService.findAccountByAccountNumberWithLock(request.accountNumber());

        account.deposit(request.amount());

        Transaction transaction = Transaction.createDeposit(
                account.getAccountNumber(),
                request.amount(),
                account.getBalance()
        );
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Deposit completed: account={}, amount={}", account.getAccountNumber(), request.amount());
        return new TransactionResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest request) {
        Account account = accountService.findAccountByAccountNumberWithLock(request.accountNumber());

        // Check daily withdrawal limit
        LocalDate today = LocalDate.now();
        DailyLimit dailyLimit = dailyLimitRepository
                .findByAccountNumberAndTransactionDateWithLock(request.accountNumber(), today)
                .orElseGet(() -> new DailyLimit(request.accountNumber(), today));

        BigDecimal newWithdrawalAmount = dailyLimit.getWithdrawalAmount().add(request.amount());
        if (newWithdrawalAmount.compareTo(DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);
        }

        // Withdraw from account
        account.withdraw(request.amount());
        dailyLimit.addWithdrawalAmount(request.amount());
        dailyLimitRepository.save(dailyLimit);

        Transaction transaction = Transaction.createWithdrawal(
                account.getAccountNumber(),
                request.amount(),
                account.getBalance()
        );
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Withdrawal completed: account={}, amount={}", account.getAccountNumber(), request.amount());
        return new TransactionResponse(savedTransaction);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.sourceAccountNumber().equals(request.targetAccountNumber())) {
            throw new BusinessException(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }

        // Lock accounts in consistent order to prevent deadlock
        String firstLock = request.sourceAccountNumber().compareTo(request.targetAccountNumber()) < 0
                ? request.sourceAccountNumber() : request.targetAccountNumber();
        String secondLock = firstLock.equals(request.sourceAccountNumber())
                ? request.targetAccountNumber() : request.sourceAccountNumber();

        accountService.findAccountByAccountNumberWithLock(firstLock);
        accountService.findAccountByAccountNumberWithLock(secondLock);

        Account sourceAccount = accountService.findAccountByAccountNumber(request.sourceAccountNumber());
        Account targetAccount = accountService.findAccountByAccountNumber(request.targetAccountNumber());

        // Calculate fee
        BigDecimal fee = request.amount().multiply(TRANSFER_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = request.amount().add(fee);

        // Check daily transfer limit (amount + fee)
        LocalDate today = LocalDate.now();
        DailyLimit dailyLimit = dailyLimitRepository
                .findByAccountNumberAndTransactionDateWithLock(request.sourceAccountNumber(), today)
                .orElseGet(() -> new DailyLimit(request.sourceAccountNumber(), today));

        BigDecimal newTransferAmount = dailyLimit.getTransferAmount().add(request.amount());
        if (newTransferAmount.compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED);
        }

        // Execute transfer
        sourceAccount.withdraw(totalDeduction);
        targetAccount.deposit(request.amount());
        dailyLimit.addTransferAmount(request.amount());
        dailyLimitRepository.save(dailyLimit);

        // Record transactions
        Transaction transferOut = Transaction.createTransferOut(
                sourceAccount.getAccountNumber(),
                request.amount(),
                sourceAccount.getBalance(),
                targetAccount.getAccountNumber(),
                fee
        );

        Transaction transferIn = Transaction.createTransferIn(
                targetAccount.getAccountNumber(),
                request.amount(),
                targetAccount.getBalance(),
                sourceAccount.getAccountNumber()
        );

        transactionRepository.save(transferOut);
        transactionRepository.save(transferIn);

        log.info("Transfer completed: from={}, to={}, amount={}, fee={}",
                sourceAccount.getAccountNumber(), targetAccount.getAccountNumber(),
                request.amount(), fee);

        return new TransferResponse(
                sourceAccount.getAccountNumber(),
                targetAccount.getAccountNumber(),
                request.amount(),
                fee,
                sourceAccount.getBalance(),
                targetAccount.getBalance()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        // Verify account exists
        accountService.findAccountByAccountNumber(accountNumber);

        List<Transaction> transactions = transactionRepository
                .findByAccountNumberOrderByCreatedAtDesc(accountNumber);

        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
}