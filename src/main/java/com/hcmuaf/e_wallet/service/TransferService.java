package com.hcmuaf.e_wallet.service;

import com.hcmuaf.e_wallet.config.VNPayConfig;
import com.hcmuaf.e_wallet.constant.StatusTransaction;
import com.hcmuaf.e_wallet.constant.Type;
import com.hcmuaf.e_wallet.dto.TransferRequest;
import com.hcmuaf.e_wallet.entity.Transaction;
import com.hcmuaf.e_wallet.entity.Wallet;
import com.hcmuaf.e_wallet.repository.TransactionalRepository;
import com.hcmuaf.e_wallet.repository.UserRepository;
import com.hcmuaf.e_wallet.repository.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferService {
    private final VNPayConfig vnPayConfig;
    private final TransactionalRepository transactionalRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Transactional
    public void transfer(TransferRequest transferRequest, HttpServletRequest request) {
        //logic transfer money between wallets
        log.info("Start transfer money between wallets");
        //1. validate transferRequest
        Long userId = request.getHeader("userId") != null ? Long.parseLong(request.getHeader("userId")) : null;
//        Long userId = 1L; //for demo purpose, we will use a fixed userId
        Long targetId = transferRequest.getTargetId();
        BigDecimal amount = BigDecimal.valueOf(transferRequest.getAmount());
        //cannot transfer to yourself
        if (targetId.equals(userId)) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }
        //find source wallet
        Long senderWalletId = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Source wallet not found"))
                .getId();
        //find target wallet
        Long targetWalletId = walletRepository.findByUserId(targetId)
                .orElseThrow(() -> new RuntimeException("Target wallet not found"))
                .getId();

        //lock ordering to prevent deadlock
        Wallet senderWallet;
        Wallet receiverWallet;

        if (senderWalletId < targetWalletId) {
            senderWallet = walletRepository.findByIdForUpdate(senderWalletId).orElseThrow(() -> new RuntimeException("Source wallet not found"));
            receiverWallet = walletRepository.findByIdForUpdate(targetWalletId).orElseThrow(() -> new RuntimeException("Target wallet not found"));
        } else {
            receiverWallet = walletRepository.findByIdForUpdate(targetWalletId).orElseThrow(() -> new RuntimeException("Target wallet not found"));
            senderWallet = walletRepository.findByIdForUpdate(senderWalletId).orElseThrow(() -> new RuntimeException("Source wallet not found"));
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        log.info("Transfer money successfully");

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .type(Type.TRANSFER)
                .status(StatusTransaction.SUCCESS)
                .sourceWallet(senderWallet)
                .targetWallet(receiverWallet)
                .content(transferRequest.getMessage() != null ? transferRequest.getMessage() : "Transfer money P2P")
                .build();

        transactionalRepository.save(transaction);
        log.info("Transfer successful form Wallet {} to Wallet {}", senderWalletId, targetWalletId);
    }
}
