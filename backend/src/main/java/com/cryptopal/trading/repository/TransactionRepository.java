package com.cryptopal.trading.repository;

import com.cryptopal.trading.entity.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
