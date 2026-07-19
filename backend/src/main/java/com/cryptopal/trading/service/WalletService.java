package com.cryptopal.trading.service;

import com.cryptopal.auth.entity.User;
import com.cryptopal.auth.entity.Wallet;
import com.cryptopal.auth.repository.UserRepository;
import com.cryptopal.auth.repository.WalletRepository;
import com.cryptopal.trading.entity.CashTransaction;
import com.cryptopal.trading.exception.InsufficientFundsException;
import com.cryptopal.trading.exception.RecipientNotFoundException;
import com.cryptopal.trading.repository.CashTransactionRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CashTransactionRepository cashTransactionRepository;

    public WalletService(WalletRepository walletRepository,
                         UserRepository userRepository,
                         CashTransactionRepository cashTransactionRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.cashTransactionRepository = cashTransactionRepository;
    }

    @Transactional
    public BigDecimal deposit(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));
        wallet.setCashBalance(wallet.getCashBalance().add(amount));
        walletRepository.save(wallet);
        cashTransactionRepository.save(new CashTransaction(userId, "DEPOSIT", null, amount));
        return wallet.getCashBalance();
    }

    @Transactional
    public BigDecimal transfer(Long fromUserId, String toUsername, BigDecimal amount) {
        User recipient = userRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RecipientNotFoundException(toUsername));
        if (recipient.getId().equals(fromUserId)) {
            throw new IllegalArgumentException("You cannot send money to yourself");
        }

        User sender = userRepository.findById(fromUserId)
                .orElseThrow(() -> new IllegalStateException("Sender not found " + fromUserId));
        Wallet from = walletRepository.findByUserId(fromUserId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + fromUserId));
        if (from.getCashBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        Wallet to = walletRepository.findByUserId(recipient.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + recipient.getId()));

        from.setCashBalance(from.getCashBalance().subtract(amount));
        to.setCashBalance(to.getCashBalance().add(amount));
        walletRepository.save(from);
        walletRepository.save(to);

        cashTransactionRepository.save(new CashTransaction(fromUserId, "SEND", recipient.getUsername(), amount));
        cashTransactionRepository.save(new CashTransaction(recipient.getId(), "RECEIVE", sender.getUsername(), amount));

        return from.getCashBalance();
    }
}
