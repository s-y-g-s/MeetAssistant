package com.treemoon.MeetAssist.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SignGenerate {

    public static String getSign(String key, String secret) {
        try {
            long timestamp = System.currentTimeMillis();
            String data = String.format("%d\n%s\n%s", timestamp, secret, key);
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] sign = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return String.format("%d%s", timestamp, new String(new org.apache.commons.codec.binary.Base64().encode(sign), StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate sign", e);
        }
    }
}