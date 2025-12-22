package com.hcmuaf.e_wallet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDTO {
    private BigDecimal amount;
    private String content;
}

