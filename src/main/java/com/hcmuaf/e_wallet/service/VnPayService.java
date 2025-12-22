package com.hcmuaf.e_wallet.service;

import com.hcmuaf.e_wallet.config.VNPayConfig;
import com.hcmuaf.e_wallet.constant.StatusTransaction;
import com.hcmuaf.e_wallet.constant.Type;
import com.hcmuaf.e_wallet.dto.response.PaymentResponse;
import com.hcmuaf.e_wallet.dto.response.VnPayIPNResponse;
import com.hcmuaf.e_wallet.entity.Transaction;
import com.hcmuaf.e_wallet.entity.Wallet;
import com.hcmuaf.e_wallet.repository.TransactionalRepository;
import com.hcmuaf.e_wallet.repository.WalletRepository;
import com.hcmuaf.e_wallet.util.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VnPayService {
    private final VNPayConfig vnPayConfig;
    //    private String ipAddress = "";
    private final TransactionalRepository transactionalRepository;
    private final WalletRepository walletRepository;


    public PaymentResponse createPaymentUrl(HttpServletRequest request) {
        //get parameters from request
        long amount = Long.parseLong(request.getParameter("amount")) * 100; // VNPay requires amount in smallest currency unit
        String bankCode = request.getParameter("bankCode");
        String ipAddress = request.getRemoteAddr();
        Long userId = request.getParameter("userId") != null ? Long.parseLong(request.getParameter("userId")) : null;
        //get user wallet but for demo purpose, we will use a fixed userId
//        Long userId = 1L;
        Wallet userWallet = walletRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User wallet not found"));
        log.info("User wallet found: " + userWallet.getId());
        log.info("Wallet balance: " + userWallet.getBalance());
        log.info("USer wallet currency: " + userWallet);

        //generate payment code
        //Format: VNP_Time_Random
        String vnp_TxnRef = vnPayConfig.getCurrentTime() + "_" + new Random().nextInt(10000);
        //save transaction with status PENDING
        //save transaction before redirect to VNPay
        Transaction transaction = Transaction.builder()
                .status(StatusTransaction.PENDING)
                .type(Type.DEPOSIT)
                .amount(BigDecimal.valueOf(amount / 100)) //store amount original currency unit
                .providerRefId(vnp_TxnRef)
                .content("Deposit for cinema") //get content from request or set default
                .sourceWallet(null)
                .targetWallet(userWallet)
                .build();

        transactionalRepository.save(transaction);

        //build parameters for VNPay
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVnp_Version());
        vnp_Params.put("vnp_Command", vnPayConfig.getVnp_Command());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Nap tien: " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOtherType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", vnPayConfig.getCurrentTime());
        vnp_Params.put("vnp_ExpireDate", vnPayConfig.getExpireTime());

        if (bankCode != null) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        //create a complete payment url
        String paymentUrl = this.buildPaymentUrl(vnp_Params);

        return PaymentResponse.builder()
                .status(StatusTransaction.SUCCESS)
                .message("Successfully generated payment URL")
                .url(paymentUrl)
                .build();
    }

    @Transactional
    public VnPayIPNResponse processVnPayIpn(HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();

        for (Enumeration<String> parameterNames = request.getParameterNames(); parameterNames.hasMoreElements(); ) {
            String fieldName = parameterNames.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                vnp_Params.put(fieldName, fieldValue);
            }
        }
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        log.info("VNPAY IPN PARAMS: " + vnp_Params);
        if (vnp_Params.containsKey("vnp_SecureHashType")) {
            vnp_Params.remove("vnp_SecureHashType");
        }
        if (vnp_Params.containsKey("vnp_SecureHash")) {
            log.info("vnp_SecureHash: " + vnp_Params.get("vnp_SecureHash"));
            vnp_Params.remove("vnp_SecureHash");
        }
        //get secure hash to compare
        log.info("vnp_SecureHash: " + vnp_Params.get("vnp_SecureHash"));

        String signValue = VnPayUtil.hashAllFields(vnp_Params, vnPayConfig.getVnpHashSecret());
        log.info("VNPAY SIGN VALUE: " + signValue);
        if (!signValue.equals(vnp_SecureHash)) {
            log.error("Vnp Secure Hash Mismatch");
            return new VnPayIPNResponse("97", "Invalid checksum");
        }
        String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");
        //where is vnp_ResponseCode?
        String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");
        String vnp_Amount = vnp_Params.get("vnp_Amount");

        Transaction transaction = transactionalRepository.findByProviderRefId(vnp_TxnRef);
        if (transaction == null) {
            return new VnPayIPNResponse("01", "Transaction not found");
        }

        long amountInDb = transaction.getAmount().longValue() * 100;
        long amountInVnPay = Long.parseLong(vnp_Amount);

        if (amountInDb != amountInVnPay) {
            //check amount of transaction if not match return error (when hacker change amount in request)
            return new VnPayIPNResponse("04", "Invalid amount");
        }
        //check transaction status if already success then return error to avoid duplicate credit
        if (transaction.getStatus().equals(StatusTransaction.SUCCESS)) {
            return new VnPayIPNResponse("02", "Transaction already confirmed");
        }

        if ("00".equals(vnp_ResponseCode)) {
            Wallet targetWallet = transaction.getTargetWallet();
            targetWallet.setBalance(targetWallet.getBalance().add(transaction.getAmount()));
            walletRepository.save(targetWallet);

            transaction.setStatus(StatusTransaction.SUCCESS);
            transactionalRepository.save(transaction);
            return new VnPayIPNResponse("00", "Transaction successful");
        } else {
            transaction.setStatus(StatusTransaction.FAILED);
            transactionalRepository.save(transaction);
            return new VnPayIPNResponse("03", "Transaction failed");
        }
    }

    private String buildPaymentUrl(Map<String, String> vnpParams) {
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        String vnp_SecureHash = "";

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                try {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append("=");
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append("=");
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        query.append("&");
                        hashData.append("&");
                    }
                } catch (Exception e) {
                    log.error("Error while encoding URL parameters", e);
                    e.printStackTrace();
                }
            }
        }
        String queryUrl = query.toString();
        try {
            vnp_SecureHash = VnPayUtil.hmacSha512(vnPayConfig.getVnpHashSecret(), hashData.toString());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return vnPayConfig.getVnpUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
    }
}
