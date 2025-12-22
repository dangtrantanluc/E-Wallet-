package com.hcmuaf.e_wallet.dto.response;

import com.hcmuaf.e_wallet.constant.StatusTransaction;
import lombok.Builder;
import lombok.*;

@Getter
@Setter
@Builder
public class PaymentResponse {
    public StatusTransaction status;
    public String message;
    public String url;
}