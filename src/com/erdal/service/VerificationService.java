package com.erdal.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class VerificationService {

    // Email → Code eşleşmesi
    private static final Map<String, String> verificationCodes = new HashMap<>();

    // Gmail veya farklı sağlayıcılar için SMTP ayarları
    private static final String SMTP_HOST = "smtp.gmail.com"; // istersen outlook veya yandex olarak değiştirebiliriz
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "seninmailin@gmail.com"; // buraya kendi gönderen mailini yaz
    private static final String FROM_PASSWORD = "uygulama_sifren"; // Gmail'de uygulama şifresi oluşturmalısın

    /**
     * E-posta adresine 6 haneli kod gönderir
     */
    public static boolean sendVerificationCode(String toEmail) {
        try {
            String code = generateCode();
            verificationCodes.put(toEmail, code);

            // SMTP yapılandırması
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("E-posta Doğrulama Kodu");
            message.setText("Selam! 👋\n\nDoğrulama kodun: " + code + "\n\nBu kod 5 dakika geçerlidir.");

            Transport.send(message);
            System.out.println("✅ Kod gönderildi: " + toEmail + " --> " + code);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Girilen kodu kontrol eder
     */
    public static boolean verifyCode(String email, String code) {
        if (!verificationCodes.containsKey(email)) return false;

        String correctCode = verificationCodes.get(email);
        boolean isValid = correctCode.equals(code);

        if (isValid) {
            verificationCodes.remove(email); // kodu kullanıldıktan sonra sil
        }

        return isValid;
    }

    /**
     * 6 haneli rastgele sayı üretir
     */
    private static String generateCode() {
        Random random = new Random();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }
}
