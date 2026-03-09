package com.jhanvi857.coreHTTP.app.repository;

import com.jhanvi857.coreHTTP.app.model.Task;
import com.jhanvi857.coreHTTP.db.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {

    public List<Task> findAll() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Connection con = DatabaseManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }
        return tasks;
    }

    public Optional<Task> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection con = DatabaseManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Task save(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (title, description, completed) VALUES (?, ?, ?) RETURNING id";
        try (Connection con = DatabaseManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setBoolean(3, task.isCompleted());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    task.setId(rs.getLong(1));
                }
            }
        }
        return task;
    }

    public boolean update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, completed = ? WHERE id = ?";
        try (Connection con = DatabaseManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setBoolean(3, task.isCompleted());
            ps.setLong(4, task.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection con = DatabaseManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        return new Task(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("completed"));
    }
}
