package com.erdal.controller;

import com.erdal.model.Login;
import com.erdal.model.User;
import com.erdal.repository.LoginRepository;

import java.util.Scanner;

public class LoginController {

    private final LoginRepository loginRepo = new LoginRepository();
    private final Scanner sc = new Scanner(System.in);
    private String currentUserId;

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void start() {
        System.out.println("=== Login ===");
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Şifre: ");
        String password = sc.nextLine();

        Login login = new Login(email, password);
        User user = loginRepo.authenticate(login);

        if (user != null) {
            currentUserId = user.getId();
            System.out.println("✅ Giriş başarılı! Hoşgeldin, " + user.getFullName());
        } else {
            System.out.println(" Geçersiz email veya şifre");
        }
    }
}
