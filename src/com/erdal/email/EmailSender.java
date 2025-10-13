package com.erdal.email;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender {

    private final String host;       // SMTP sunucu (ör: smtp.gmail.com, smtp.office365.com)
    private final int port;          // SMTP port (587 genellikle)
    private final String username;   // E-posta adresi
    private final String password;   // SMTP şifresi veya uygulama şifresi

    public EmailSender(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public  boolean sendVerificationCode(String toEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "Task App"));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Task App Doğrulama Kodu");
            message.setText("Merhaba! Kaydınızı tamamlamak için doğrulama kodunuz: " + code);

            Transport.send(message);
            System.out.println("Doğrulama kodu gönderildi: " + toEmail);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
