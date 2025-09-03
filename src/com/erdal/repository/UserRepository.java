package com.erdal.repository;

import com.erdal.databaseConnection.DatabaseConnection;
import com.erdal.model.User;
import com.erdal.methods.MethodService;
import com.erdal.methods.Methods;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final MethodService methodService = new Methods();

    // Constructor’da tablo kontrolü
    public UserRepository() {
        ensureTable();
    }

    // Tablo yoksa oluştur
    private void ensureTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(8) PRIMARY KEY,
                full_name VARCHAR(255) NOT NULL,
                phone VARCHAR(50),
                gender VARCHAR(10),
                address TEXT,
                email VARCHAR(255) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = DatabaseConnection.connect();
             Statement st = conn.createStatement()) {
            st.execute(sql);
            System.out.println("✅ Users tablosu hazır.");
        } catch (SQLException e) {
            System.out.println("❌ Users tablosu oluşturulamadı: " + e.getMessage());
        }
    }

    // Kullanıcı ekleme
    public void register(User user) {
        String sql = "INSERT INTO users (id, full_name, phone, gender, address, email, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String userId = methodService.generateUserId();
            ps.setString(1, userId);
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getGender());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getEmail());
            ps.setString(7, user.getPassword());

            ps.executeUpdate();
            System.out.println("Kullanıcı eklendi: " + user.getFullName() + " (ID: " + userId + ")");

        } catch (SQLException e) {
            System.out.println("Kullanıcı ekleme hatası: " + e.getMessage());
        }
    }

    // Email + Password ile login
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            System.out.println("Login hatası: " + e.getMessage());
        }
        return null;
    }

    // Tüm kullanıcıları listele
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name";

        try (Connection conn = DatabaseConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                list.add(user);
            }

        } catch (SQLException e) {
            System.out.println("Listeleme hatası: " + e.getMessage());
        }
        return list;
    }
}
