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

    // constructor table control
    public UserRepository() {
        ensureTable();
    }

    // If no table, create one
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
            System.out.println(" User table is ready.");
        } catch (SQLException e) {
            System.out.println("Could not create users table : " + e.getMessage());
        }
    }

    // adding user
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
            System.out.println("User added: " + user.getFullName() + " (ID: " + userId + ")");

        } catch (SQLException e) {
            System.out.println("Adding user error: " + e.getMessage());
        }
    }

    //Login by Email + Password 
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
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // List all users
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
            System.out.println("List error: " + e.getMessage());
        }
        return list;
    }

    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
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
            System.out.println("findById error: " + e.getMessage());
        }
        return null;
    }
    
    public boolean updateField(String id, String field, String value) {
        String column;
        switch (field) {
            case "fullName" -> column = "full_name";
            case "phone" -> column = "phone";
            case "gender" -> column = "gender";
            case "address" -> column = "address";
            default -> {
                System.out.println(" Invalid field: " + field);
                return false;
            }
        }

        String sql = "UPDATE users SET " + column + " = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, id);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.out.println(" updateField error: " + e.getMessage());
            return false;
        }
    }
    
    public String getUserEmailById(String userId) {
        String sql = "SELECT email FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }

        } catch (SQLException e) {
            System.out.println(" Email alınamadı: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteUserById(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("️ User deleted: " + userId);
                return true;
            } else {
                System.out.println("️ User not found: " + userId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
            return false;
        }
    }


}
