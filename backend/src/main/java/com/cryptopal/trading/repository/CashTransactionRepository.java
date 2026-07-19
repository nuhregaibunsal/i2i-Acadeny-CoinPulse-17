package com.cryptopal.trading.repository;

import com.cryptopal.trading.entity.CashTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {

    List<CashTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
