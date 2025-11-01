package com.erdal.controller;

import com.erdal.model.User;
import com.erdal.repository.UserRepository;

import java.util.List;
import java.util.Scanner;


public class UserController {

    private final UserRepository repo = new UserRepository();
    private final Scanner sc = new Scanner(System.in);
    private String currentUserId; // login sonrası userId

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== User Manager ===");
            System.out.println("1) Kullanıcı Ekle");
            System.out.println("2) Kullanıcıları Listele");
            System.out.println("3) Login");
            System.out.println("4) Kullanıcı Güncelle");
            System.out.println("5) Çıkış");
            System.out.print("Seçim: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> addUser();
                case "2" -> listUsers();
                case "3" -> login();
                case "4" -> updateUser();
                case "5" -> {
                    System.out.println("Görüşürüz!");
                    return;
                }
                default -> System.out.println("Geçersiz seçim");
            }
        }
    }

    // Kullanıcı ekleme
    private void addUser() {
        System.out.print("Ad Soyad: ");
        String fullName = sc.nextLine();
        System.out.print("Telefon: ");
        String phone = sc.nextLine();
        System.out.print("Cinsiyet: ");
        String gender = sc.nextLine();
        System.out.print("Adres: ");
        String address = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Şifre: ");
        String password = sc.nextLine();

        User user = new User(fullName, phone, gender, address, email, password);
        repo.register(user);
    }

    // Kullanıcı listeleme
    private void listUsers() {
       
       User user=login();
       
       if (user == null) {
           System.out.println("Giriş başarısız, kullanıcı listesine erişilemez.");
           return;
       }
       
       if (user.isAdmin()) {
    	   List<User> users = repo.findAll();
    	   if (users.isEmpty()) {
               System.out.println(" Kayıt yok.");
           } else {
               users.forEach(u -> System.out.println(u.getId() + " | " + u.getFullName() + " | " + u.getEmail()));
           }
		
	}else {
		System.out.println(user.getFullName()+" yetki alani disinda");
	}
       
    }
    
    

    // Login
    private User login() {
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Şifre: ");
        String password = sc.nextLine();

        User user = repo.login(email, password);
        if (user != null) {
            currentUserId = user.getId();
            
            System.out.println(" Giriş başarılı! Hoşgeldin, " + user.getFullName());
        } else {
            System.out.println("Geçersiz email veya şifre");
        }
        return user;
    }

    // Kullanıcı güncelleme
    private void updateUser() {
        System.out.print("Güncellenecek Kullanıcı ID: ");
        String id = sc.nextLine();

        System.out.println("Hangi alanı güncellemek istiyorsun? (fullName/phone/gender/address)");
        String field = sc.nextLine();

        System.out.print("Yeni değer: ");
        String value = sc.nextLine();

        boolean success = repo.updateField(id, field, value);
        if (success) {
            System.out.println(" Güncelleme başarılı!");
        } else {
            System.out.println(" Güncelleme başarısız.");
        }
       
    }
    //Delete user
    @SuppressWarnings("unused")
	private void deleteUser() {
    	System.out.print("Silinecek Kullanıcı ID: ");
        String id = sc.nextLine();
        
        boolean success=repo.deleteUserById(id);
        if (success) {
            System.out.println(" Silme başarılı!");
        } else {
            System.out.println(" Silme başarısız.");
        }
    }
}
