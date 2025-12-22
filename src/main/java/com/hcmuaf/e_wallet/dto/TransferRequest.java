package com.hcmuaf.e_wallet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    @NotNull
    private Long targetId;

    @NotNull
    @Min(value = 1000, message = "Amount must be at least 1000")
    private Long amount;

    private String message;
}
