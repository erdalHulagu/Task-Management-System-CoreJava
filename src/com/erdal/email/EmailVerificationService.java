package com.erdal.email;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EmailVerificationService {

 // Email -> [code, expireTime] eşlemesi
 private static final Map<String, CodeEntry> codeMap = new HashMap<>();

 private static class CodeEntry {
     String code;
     long expireTime; // millis cinsinden

     CodeEntry(String code, long expireTime) {
         this.code = code;
         this.expireTime = expireTime;
     }
 }

 // 6 haneli rastgele kod üret
 public static String generateCode() {
     Random r = new Random();
     int number = 100000 + r.nextInt(900000);
     return String.valueOf(number);
 }

 // Email ve kodu sakla, expire süresi 5 dk
 public static void saveCode(String email, String code) {
     long expireTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5 dakika
     codeMap.put(email, new CodeEntry(code, expireTime));
 }

 // Girilen kodu doğrula
 public static boolean verifyCode(String email, String code) {
     CodeEntry entry = codeMap.get(email);
     if (entry != null && entry.code.equals(code)) {
         if (System.currentTimeMillis() <= entry.expireTime) {
             codeMap.remove(email); // doğrulandıktan sonra sil
             return true;
         } else {
             codeMap.remove(email); // süresi dolmuş kodu sil
             return false;
         }
     }
     return false;
 }
}
