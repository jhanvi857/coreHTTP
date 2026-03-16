package com.jhanvi857.taskplanner.repository;

import com.jhanvi857.taskplanner.model.Task;
import com.jhanvi857.taskplanner.db.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    // Dedicated db thread pool for JDBC IO since standard Java JDBC is blocking
    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(10);

    public CompletableFuture<List<Task>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<Task> tasks = new ArrayList<>();
            String sql = "SELECT * FROM tasks";
            try (Connection con = DatabaseManager.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            } catch (SQLException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
            return tasks;
        }, dbExecutor);
    }

    public CompletableFuture<Optional<Task>> findById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM tasks WHERE id = ?";
            try (Connection con = DatabaseManager.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToTask(rs));
                    }
                }
            } catch (SQLException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
            return Optional.empty();
        }, dbExecutor);
    }

    public CompletableFuture<Task> save(Task task) {
        return CompletableFuture.supplyAsync(() -> {
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
            } catch (SQLException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
            return task;
        }, dbExecutor);
    }

    public CompletableFuture<Boolean> update(Task task) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE tasks SET title = ?, description = ?, completed = ? WHERE id = ?";
            try (Connection con = DatabaseManager.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setBoolean(3, task.isCompleted());
                ps.setLong(4, task.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, dbExecutor);
    }

    public CompletableFuture<Boolean> delete(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM tasks WHERE id = ?";
            try (Connection con = DatabaseManager.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, dbExecutor);
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        return new Task(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("completed"));
    }
}
