package com.hcmuaf.e_wallet.service;

import com.hcmuaf.e_wallet.constant.Role;
import com.hcmuaf.e_wallet.constant.StatusUser;
import com.hcmuaf.e_wallet.entity.User;
import com.hcmuaf.e_wallet.entity.Wallet;
import com.hcmuaf.e_wallet.repository.UserRepository;
import com.hcmuaf.e_wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    //when we add a new user, we need to consider role of user default is USER
    //status default is ACTIVE
    //we also need to create a wallet for user when we create a new user with balance = 0
    public void saveUsers(User user) {
        log.info("Initialize new user with default role and status, create wallet for user with balance 0");
        if (user.getRole() == null) user.setRole(Role.CUSTOMER);
        if (user.getStatus() == null) user.setStatus(StatusUser.ACTIVE);

        Wallet wallet = Wallet.builder()
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .user(user)
                .build();

        user.setWallet(wallet);
        userRepository.save(user);
        log.info("Save new user into database");
    }

}
