package com.erdal;

import com.erdal.server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            // 🔹 Server başlat
            new Server().start();
        } catch (Exception e) {
            System.out.println("❌ Server başlatılamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
