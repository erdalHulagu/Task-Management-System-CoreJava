package com.erdal.controller;

import com.erdal.model.Task;                
import com.erdal.repository.TaskRepository;

import java.util.List;
import java.util.Scanner;

public class TaskController {

    private final TaskRepository repo = new TaskRepository(); //  Repository hazır
    private final Scanner sc = new Scanner(System.in);        // 🔹
    //  Ana menü başlat
    public void start() {
        while (true) {
            System.out.println("\n=== Task Manager ===");
            System.out.println("1) Ekle");
            System.out.println("2) Listele");
            System.out.println("3) Sil");
            System.out.println("4) Güncelle");
            System.out.println("5) Çık");
            System.out.print("Seçim: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> add();       // Ekle
                case "2" -> list();      // Listele
                case "3" -> delete();    // Sil
                case "4" -> update();    // Güncelle
                case "5" -> { 
                    System.out.println("Görüşürüz!"); 
                    return; 
                }
                default -> System.out.println(" Geçersiz seçim");
            }
        }
    }

    // 🔹 Task ekleme
    private void add() {
        System.out.print("Başlık: ");
        String title = sc.nextLine();
        System.out.print("Açıklama: ");
        String desc = sc.nextLine();
        repo.add(new Task(title, desc));
    }

    // 🔹 Task listeleme
    private void list() {
        List<Task> tasks = repo.findAll();
        if (tasks.isEmpty()) {
            System.out.println(" Kayıt yok.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    // 🔹 Task silme
    private void delete() {
        System.out.print("Silinecek ID: ");
        try {
            int id = Integer.parseInt(sc.nextLine());
            repo.deleteById(id);
        } catch (NumberFormatException e) {
            System.out.println(" Lütfen sayı giriniz.");
        }
    }

    // 🔹 Task güncelleme (sadece başlık)
    private void update() {
        System.out.print("Güncellenecek ID: ");
        try {
            int id = Integer.parseInt(sc.nextLine());
            System.out.print("Yeni Başlık: ");
            String newTitle = sc.nextLine();
            boolean ok = repo.updateTitle(id, newTitle);
            System.out.println(ok ? " Güncellendi" : "!!!️ ID bulunamadı");
        } catch (NumberFormatException e) {
            System.out.println(" Lütfen sayı giriniz.");
        }
    }
}
