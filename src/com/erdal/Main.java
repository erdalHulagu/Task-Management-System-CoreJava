package com.erdal;

import com.erdal.server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            new Server().start(); // HTTP sunucuyu başlat
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
