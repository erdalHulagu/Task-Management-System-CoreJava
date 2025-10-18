package com.erdal.service;

import com.erdal.databaseConnection.DatabaseConnection;
import com.erdal.model.Task;
import com.erdal.repository.TaskRepository;
import com.erdal.repository.UserRepository;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class TaskReminderService {

    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();

    private static final String FROM_EMAIL = "erdalhulahu@gmail.com";
    private static final String APP_PASSWORD = "fgwl dmhy xzrm hvxs";
    
    
    public void startDailyReminder() {
        Timer timer = new Timer(true);

        TimerTask dailyTask = new TimerTask() {
            @Override
            public void run() {
                LocalDate today = LocalDate.now();

                // Bugünün görevlerini al ve e-posta gönder
                List<Task> tasks = taskRepository.findTasksByDate(today);

                for (Task task : tasks) {
                    String email = userRepository.getUserEmailById(task.getUserId());
                    if (email != null) {
                        sendEmail(email, task);
                    }
                }

                // 2 günden eski görevleri sil
                LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
                String sql = "DELETE FROM tasks WHERE taskTime < ?";
                try (var conn = DatabaseConnection.connect();
                     var ps = conn.prepareStatement(sql)) {

                    ps.setObject(1, threeDaysAgo);
                    int deleted = ps.executeUpdate();

                    if (deleted > 0) {
                        System.out.println( deleted + " adet eski görev silindi (3 günden eski).");
                    }
                } catch (Exception e) {
                    System.out.println("Temizlik hatası: " + e.getMessage());
                }
            }
        };

        // 3️ — Gerçek kullanım: her 24 saatte bir çalışır
        long delay = 0;
        long period = 24 * 60 * 60 * 1000;
        timer.scheduleAtFixedRate(dailyTask, delay, period);

        // 🔹 TEST için (örneğin 5 saniyede bir çalışsın)
        // long delay = 5000;
        // long period = 15000;
        // timer.scheduleAtFixedRate(dailyTask, delay, period);
    }


    private void sendEmail(String toEmail, Task task) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🕊️ Daily Planning Reminder ");

            // HTML ile kart görünümü
            String htmlContent = """
                <div style="font-family: Poppins, sans-serif; max-width: 600px; margin: 0 auto; border-radius: 12px; background: #e0f0ff; border-left: 5px solid #1e3c72; padding: 20px; color: #1e3c72;">
                    <h2 style="margin-top: 0;">🗂️ %s</h2>
                    <p style="font-size: 14px; margin: 8px 0;">%s</p>
                    <p style="font-size: 13px; color: #115a8a; margin: 10px 0;">🗓️ %s</p>
                    <hr style="border: 0; border-top: 1px solid #1e3c72; margin: 10px 0;">
                    <p style="font-size: 12px; color: #1e3c72;">Have a nice day, InshaAllah!</p>
                </div>
                """.formatted(
                    task.getTitle(),
                    task.getDescription() != null ? task.getDescription() : "",
                    task.getTaskTime() != null ? task.getTaskTime() : ""
                );

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("📧 Hatırlatma e-postası gönderildi: " + toEmail);
        } catch (MessagingException e) {
            System.out.println(" Email gönderilemedi: " + e.getMessage());
        }
    }
}
