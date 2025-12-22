package com.hcmuaf.e_wallet.dto.response;

import jakarta.transaction.Transactional;
import lombok.*;

@Transactional
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VnPayIPNResponse {
    private String msgResponse;
    private String message;
}
