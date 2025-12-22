package com.hcmuaf.e_wallet.controller;

import com.hcmuaf.e_wallet.dto.TransferRequest;
import com.hcmuaf.e_wallet.dto.response.PaymentResponse;
import com.hcmuaf.e_wallet.dto.response.VnPayIPNResponse;
import com.hcmuaf.e_wallet.service.TransferService;
import com.hcmuaf.e_wallet.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    //here will be implemented payment functionalities
    //write API return direct VNPAY
    private final VnPayService vnPayService;
    private final TransferService transferService;

    @GetMapping("/vn-pay")
    public ResponseEntity<?> createVnPayPayment(HttpServletRequest request) {
        log.info("Creating VNPAY payment URL");
        PaymentResponse response = vnPayService.createPaymentUrl(request);
        log.debug("Debugging" + response.toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<?> vnPayCallback(HttpServletRequest request) {
        log.info("Creating VNPAY callback URL");
        VnPayIPNResponse response = vnPayService.processVnPayIpn(request);
        log.debug("Debugging" + response.toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferPayment(@Valid @RequestBody TransferRequest request, HttpServletRequest httpServletRequest) {
        log.info("Creating transfer payment ");
        transferService.transfer(request, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Transfer money successfully");
    }
}
