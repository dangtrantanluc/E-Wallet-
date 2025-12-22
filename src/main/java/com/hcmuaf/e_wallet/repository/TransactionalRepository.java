package com.hcmuaf.e_wallet.repository;

import com.hcmuaf.e_wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionalRepository extends JpaRepository<Transaction, Long> {
    Transaction findByProviderRefId(@Param("txnRef") String txnRef);
}
