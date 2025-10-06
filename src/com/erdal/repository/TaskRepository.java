package com.erdal.repository;

import com.erdal.databaseConnection.DatabaseConnection;
import com.erdal.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    public TaskRepository() {
        ensureTable();
    }

    private void ensureTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                user_id VARCHAR(8) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = DatabaseConnection.connect();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("Tablo oluşturulamadı: " + e.getMessage());
        }
    }

    // Task ekleme
    public void add(Task task) {
        String sql = "INSERT INTO tasks (title, description, user_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getUserId());
            ps.executeUpdate();
            System.out.println("Task eklendi: " + task.getTitle());

        } catch (SQLException e) {
            System.out.println("Ekleme hatası: " + e.getMessage());
        }
    }

    // Kullanıcıya ait tüm task'leri listele
    public List<Task> findAllByUserId(String userId) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT id, title, description,taskTime FROM tasks WHERE user_id = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("taskTime").toLocalDate()
                        userId
                );
                list.add(task);
            }

        } catch (SQLException e) {
            System.out.println("Listeleme hatası: " + e.getMessage());
        }

        return list;
    }

    // Task silme (user kontrolü ile)
    public boolean deleteById(int id, String userId) {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, userId);
            int affected = ps.executeUpdate();
            System.out.println(affected > 0 ? "Silindi: " + id : " Bulunamadı veya yetkiniz yok: " + id);
            return affected > 0;

        } catch (SQLException e) {
            System.out.println("Silme hatası: " + e.getMessage());
            return false;
        }
    }

    // Task güncelleme (sadece başlık, user kontrolü ile)
    public boolean updateTitle(int id, String newTitle, String userId) {
        String sql = "UPDATE tasks SET title = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newTitle);
            ps.setInt(2, id);
            ps.setString(3, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
}
