package com.hcmuaf.e_wallet.service;

import com.hcmuaf.e_wallet.entity.Wallet;
import com.hcmuaf.e_wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public Wallet saveWallet(Wallet wallet) {
        log.info("Save new wallet into database");
        return walletRepository.save(wallet);
    }
}
