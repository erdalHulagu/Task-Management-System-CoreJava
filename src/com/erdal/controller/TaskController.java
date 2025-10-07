package com.erdal.controller;

import com.erdal.model.Task;
import com.erdal.repository.TaskRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TaskController {

    private final TaskRepository repo = new TaskRepository();
    private final Scanner sc = new Scanner(System.in);

    private String currentUserId; // login sonrası hangi user ekliyor

    // Kullanıcı ID'sini set et
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== Task Manager ===");
            System.out.println("1) Görev Ekle");
            System.out.println("2) Görevleri Listele");
            System.out.println("3) Görev Sil");
            System.out.println("4) Görev Güncelle");
            System.out.println("5) Çık");
            System.out.print("Seçim: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> add();
                case "2" -> list();
                case "3" -> delete();
                case "4" -> update();
                case "5" -> {
                    System.out.println("Görüşürüz!");
                    return;
                }
                default -> System.out.println("Geçersiz seçim");
            }
        }
    }

    // Task ekleme
    private void add() {
        if (currentUserId == null) {
            System.out.println(" Önce giriş yapmalısınız!");
            return;
        }

        System.out.print("Başlık: ");
        String title = sc.nextLine();
        System.out.print("Açıklama: ");
        String desc = sc.nextLine();
        System.out.print("Tarih: ");
        String tarih = sc.nextLine();
        
        LocalDate taskTime = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            taskTime = LocalDate.parse(tarih, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Geçersiz tarih formatı! Lütfen 'yyyy-MM-dd HH:mm' formatında girin.");
            return;
        }

        Task task = new Task(title, desc, currentUserId,taskTime);
        repo.add(task);
    }

    // Task listeleme
    private void list() {
        List<Task> tasks = repo.findAllByUserId(currentUserId);
        if (tasks.isEmpty()) {
            System.out.println(" Kayıt yok.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    // Task silme
    private void delete() {
        System.out.print("Silinecek ID: ");
        try {
            int id = Integer.parseInt(sc.nextLine());
            repo.deleteById(id,currentUserId);
        } catch (NumberFormatException e) {
            System.out.println(" Lütfen sayı giriniz.");
        }
    }

    // Task güncelleme
    private void update() {
        System.out.print("Güncellenecek ID: ");
        try {
            int id = Integer.parseInt(sc.nextLine());
            System.out.print("Yeni Başlık: ");
            String newTitle = sc.nextLine();
            System.out.print("Tarih: ");
            String tarih = sc.nextLine();
            
            LocalDate taskTime = null;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                taskTime = LocalDate.parse(tarih, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Geçersiz tarih formatı! Lütfen 'yyyy-MM-dd HH:mm' formatında girin.");
                return;
            }

            
            boolean ok = repo.updateTitleAndTime(id, newTitle, taskTime,currentUserId);
            System.out.println(ok ? " Güncellendi" : "!!!️ ID bulunamadı veya yetkiniz yok");
        } catch (NumberFormatException e) {
            System.out.println(" Lütfen sayı giriniz.");
        }
    }
}
