package com.erdal.databaseConnection;


import java.sql.Connection;       // JDBC Connection
import java.sql.DatabaseMetaData; // Veritabanı bilgilerini yazdırmak için
import java.sql.DriverManager;    // Bağlantıyı açmak için
import java.sql.SQLException;     // Hata yakalamak için

public class DatabaseConnection {

    //  PostgreSQL bağlantı bilgileri (kendine göre düzenle)
    private static final String URL = "jdbc:postgresql://localhost:5432/taskdb"; // DB adı taskdb
    private static final String USER = "postgres";                   // Kullanıcı adı
    private static final String PASSWORD = "password";                        // Şifre

    //  Bağlantı metodu
    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); // Bağlantıyı aç

        //  Database bilgilerini yazdır (opsiyonel)
        try {
            DatabaseMetaData dbData = conn.getMetaData();
            System.out.println(" Connected to: " + dbData.getDatabaseProductName() +
                               " / " + dbData.getDatabaseProductVersion());
        } catch (Exception ignore) {
            // Hata olsa da yutuyoruz, metaData sadece bilgi amaçlı
        }

        return conn; // Not: Repository bunu kullanacak
    }
}
