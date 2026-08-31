package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.PayHereRequestDTO;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${payhere.secret.key}")
    private String payHereSecretKey;

    @Value("${payhere.merchant.id}")
    private String merchantId;

    /**
     * PayHere hash calculation.
     * PayHere expects: MD5(merchant_id + order_id + amount + currency + MD5(secret).toUpperCase()).toUpperCase()
     */
    public String calculatePayHereHash(PayHereRequestDTO dto) {
        try {
            // Validate inputs
            if (dto.getMerchantId() == null || dto.getMerchantId().trim().isEmpty()) {
                throw new IllegalArgumentException("Merchant ID is null or empty");
            }
            if (dto.getOrderId() == null || dto.getOrderId().trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID is null or empty");
            }
            if (dto.getCurrency() == null || dto.getCurrency().trim().isEmpty()) {
                throw new IllegalArgumentException("Currency is null or empty");
            }
            if (payHereSecretKey == null || payHereSecretKey.trim().isEmpty()) {
                throw new IllegalArgumentException("Secret key is not configured");
            }

            // 1. Format amount to EXACTLY 2 decimal places
            double rawAmount = dto.getAmount() != null ? dto.getAmount() : 0.0;
            BigDecimal amount = BigDecimal.valueOf(rawAmount).setScale(2, RoundingMode.HALF_UP);
            String amountStr = amount.toPlainString(); // e.g., "9140.00"

            // 2. Hash the secret key first
            String secretHash = DigestUtils.md5Hex(payHereSecretKey.trim());
            String secretHashUpper = secretHash.toUpperCase();

            // 3. Concatenate in exact order (no spaces)
            String concatenated = dto.getMerchantId().trim() +
                    dto.getOrderId().trim() +
                    amountStr +
                    dto.getCurrency().trim() +
                    secretHashUpper;

            // 4. Calculate final hash
            String finalHash = DigestUtils.md5Hex(concatenated);
            return finalHash.toUpperCase();

        } catch (Exception e) {
            System.err.println("Error calculating PayHere hash: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Masks secret key for secure logging
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() < 8) return "****";
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }

    /**
     * Verify PayHere notification signature
     */
    public boolean verifyPayHereNotification(Map<String, String> notificationData) {
        try {
            String receivedMd5Sig = notificationData.get("md5sig");
            if (receivedMd5Sig == null || receivedMd5Sig.trim().isEmpty()) {
                System.err.println("No md5sig in notification");
                return false;
            }

            String merchantIdParam = notificationData.get("merchant_id");
            String orderId = notificationData.get("order_id");
            String payhereAmount = notificationData.get("payhere_amount");
            String payhereCurrency = notificationData.get("payhere_currency");
            String statusCode = notificationData.get("status_code");

            if (merchantIdParam == null || orderId == null || payhereAmount == null ||
                    payhereCurrency == null || statusCode == null) {
                System.err.println("Missing required notification fields");
                return false;
            }

            if (!merchantIdParam.equals(merchantId)) {
                System.err.println("Merchant ID mismatch! Expected: " + merchantId + ", Got: " + merchantIdParam);
                return false;
            }

            // Calculate expected hash
            String secretHash = DigestUtils.md5Hex(payHereSecretKey.trim()).toUpperCase();
            String dataToHash = merchantIdParam + orderId + payhereAmount +
                    payhereCurrency + statusCode + secretHash;
            String localMd5Sig = DigestUtils.md5Hex(dataToHash).toUpperCase();

            boolean isValid = localMd5Sig.equals(receivedMd5Sig.toUpperCase());

            return isValid;

        } catch (Exception e) {
            System.err.println("Error verifying PayHere notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}