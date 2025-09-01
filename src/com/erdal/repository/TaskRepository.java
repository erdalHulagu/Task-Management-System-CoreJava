package com.erdal.repository;

import com.erdal.databaseConnection.DatabaseConnection; //  DB bağlantısı
import com.erdal.model.Task;                  //  Task model

import java.sql.*;                             //  JDBC API
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    //  CREATE TABLE (tablo yoksa oluştur)
    public TaskRepository() {
        ensureTable();
    }

    private void ensureTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = DatabaseConnection.connect();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println(" Tablo oluşturulamadı: " + e.getMessage());
        }
    }

    // 🔹 CREATE (Task ekle)
    public void add(Task task) {
        String sql = "INSERT INTO tasks (title, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.executeUpdate();
            System.out.println(" Task eklendi: " + task.getTitle());

        } catch (SQLException e) {
            System.out.println("Ekleme hatası: " + e.getMessage());
        }
    }

    // 🔹 READ (Tüm task’leri listele)
    public List<Task> findAll() {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT id, title, description, created_at FROM tasks ORDER BY id";

        try (Connection conn = DatabaseConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description")
                );
                list.add(task);
            }

        } catch (SQLException e) {
            System.out.println(" Listeleme hatası: " + e.getMessage());
        }

        return list;
    }

    // 🔹 DELETE (ID ile sil)
    public boolean deleteById(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            System.out.println(affected > 0 ? "Silindi: " + id : " Bulunamadı: " + id);
            return affected > 0;

        } catch (SQLException e) {
            System.out.println(" Silme hatası: " + e.getMessage());
            return false;
        }
    }

    // 🔹 UPDATE (Task başlığını güncelle)
    public boolean updateTitle(int id, String newTitle) {
        String sql = "UPDATE tasks SET title = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newTitle);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(" Güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
}
