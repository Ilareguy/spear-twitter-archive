package com.ilareguy.spear.util;

import android.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha1Signature {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String calculateRFC2104HMAC(String base_string, String key)
    {
        try {

            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), mac.getAlgorithm());
            mac.init(secret);
            byte[] digest = mac.doFinal(base_string.getBytes());

            // Generate
            String encoded_result =  Base64.encodeToString(digest, Base64.DEFAULT);

            // The last character here is "\n"; remove it and return
            return encoded_result.substring(0, encoded_result.length() - 1);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
