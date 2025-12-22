package com.hcmuaf.e_wallet.controller;

import com.hcmuaf.e_wallet.entity.User;
import com.hcmuaf.e_wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        log.info("Register new user");
        if (user == null) {
            log.error("User object is null");
            throw new IllegalArgumentException("User object cannot be null");
        }
        log.info("Saving new user to the database");
        userService.saveUsers(user);
        return ResponseEntity.ok(user);
    }
}
