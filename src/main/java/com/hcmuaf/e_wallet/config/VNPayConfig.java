package com.hcmuaf.e_wallet.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@Configuration
@Data
public class VNPayConfig {
    @Value("${vnpay.vnpUrl}")
    private String vnpUrl;

    @Value("${vnpay.vnpTmnCode}")
    private String vnpTmnCode;

    @Value("${vnpay.vnpHashSecret}")
    private String vnpHashSecret;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    private String vnp_Version = "2.1.0";

    private String vnp_Command = "pay";

    private String otherType = "other";

    public static String getCurrentTime(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getExpireTime(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        calendar.add(Calendar.MINUTE,15);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(calendar.getTime());
    }
}
