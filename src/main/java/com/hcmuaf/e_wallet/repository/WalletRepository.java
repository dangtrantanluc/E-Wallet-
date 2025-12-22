package com.hcmuaf.e_wallet.repository;

import com.hcmuaf.e_wallet.entity.User;
import com.hcmuaf.e_wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(@Param("userId") Long userId);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM wallets WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);
}
