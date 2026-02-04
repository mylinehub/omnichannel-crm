package com.mylinehub.crm.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtils {

    private static final String SECRET_KEY = "orgkeyis123org12"; // Must match Angular key
    private static final String INIT_VECTOR = "YourInitVector12"; // Must match Angular IV
    public static final String DELIMITER = "CryptoUtils";
    public static final String VERIFYTOKEN = "mylinehub123";
    
    public static String decrypt(String encryptedText) {
        try {
        	
        	System.out.println("Start Decrypt");
        	
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            System.out.println("After defining Iv and secret spec");
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            System.out.println("Before initializaing cipher");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            System.out.println("After initializaing cipher");
            
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedText));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}



//Code For Front End

//Javascript:
//    import * as CryptoJS from 'crypto-js';
//   
//    const key = CryptoJS.enc.Utf8.parse('YourSecretKey123'); // 16, 24 or 32 bytes
//    const iv = CryptoJS.enc.Utf8.parse('YourInitVector12'); // 16 bytes
//   
//    function encrypt(msg: string) {
//        let encrypted = CryptoJS.AES.encrypt(CryptoJS.enc.Utf8.parse(msg), key, {
//            keySize: 128 / 8,
//            iv: iv,
//            mode: CryptoJS.mode.CBC,
//            padding: CryptoJS.pad.Pkcs7
//        });
//        return encrypted.toString();
//    }
//   
//    function decrypt(msg: string) {
//        let decrypted = CryptoJS.AES.decrypt(msg, key, {
//            keySize: 128 / 8,
//            iv: iv,
//            mode: CryptoJS.mode.CBC,
//            padding: CryptoJS.pad.Pkcs7
//        });
//        return decrypted.toString(CryptoJS.enc.Utf8);
//    }
//
//
//FRONTEND
//    let token = 'sensitiveData';
//    let encryptedToken = encrypt(token);
    // Send encryptedToken to the backend
