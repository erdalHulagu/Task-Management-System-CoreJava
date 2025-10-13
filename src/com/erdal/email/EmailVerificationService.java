package com.erdal.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EmailVerificationService {

    // Email -> doğrulama kodu eşlemesi (geçici saklama)
    private static final Map<String, String> codeMap = new HashMap<>();

    // 6 haneli rastgele kod üret
    public static String generateCode() {
        Random r = new Random();
        int number = 100000 + r.nextInt(900000); // 100000-999999
        return String.valueOf(number);
    }

    // Email ve kodu sakla
    public static void saveCode(String email, String code) {
        codeMap.put(email, code);
    }

    // Girilen kodu doğrula
    public static boolean verifyCode(String email, String code) {
        String stored = codeMap.get(email);
        if (stored != null && stored.equals(code)) {
            codeMap.remove(email); // doğrulandıktan sonra sil
            return true;
        }
        return false;
    }
}
