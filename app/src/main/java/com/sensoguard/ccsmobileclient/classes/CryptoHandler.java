package com.sensoguard.ccsmobileclient.classes;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class CryptoHandler {

    //AES takes only 16 or 24 or 32 byte of key
    private static final String SECRET_KEY = "CCSMob12Key123456789012345678901";
    private static final String IV = "DetectorIv123456";

    private static CryptoHandler instance = null;

    public static synchronized CryptoHandler getInstance() {
        if (instance == null) {
            instance = new CryptoHandler();
        }
        return instance;
    }

    public String encrypt(String message) throws GeneralSecurityException {
        byte[] srcBuff = message.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.substring(0, 32).getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.substring(0, 16).getBytes(StandardCharsets.UTF_8));
        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        ecipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        byte[] dstBuff = ecipher.doFinal(srcBuff);
        return Base64.encodeToString(dstBuff, Base64.DEFAULT);
    }
}
