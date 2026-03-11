package com.jhanvi857.taskplanner.controller;

import com.jhanvi857.taskplanner.model.Task;
import com.jhanvi857.taskplanner.repository.TaskRepository;
import com.jhanvi857.nioflow.protocol.HttpStatus;
import com.jhanvi857.nioflow.routing.HttpContext;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TaskController {
    private final TaskRepository repository = new TaskRepository();

    public void list(HttpContext ctx) {
        try {
            List<Task> tasks = repository.findAll();
            ctx.status(HttpStatus.OK).json(tasks);
        } catch (SQLException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", "Database error"));
        }
    }

    public void create(HttpContext ctx) {
        Task task = ctx.body(Task.class);
        if (task == null || task.getTitle() == null) {
            ctx.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", "Title is required"));
            return;
        }
        try {
            Task saved = repository.save(task);
            ctx.status(HttpStatus.CREATED).json(saved);
        } catch (SQLException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", "Database error"));
        }
    }

    public void get(HttpContext ctx) {
        String idStr = ctx.pathParam("id");
        try {
            Long id = Long.parseLong(idStr);
            Optional<Task> task = repository.findById(id);
            if (task.isPresent()) {
                ctx.status(HttpStatus.OK).json(task.get());
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", "Task not found"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", "Invalid ID format"));
        } catch (SQLException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", "Database error"));
        }
    }

    public void delete(HttpContext ctx) {
        String idStr = ctx.pathParam("id");
        try {
            Long id = Long.parseLong(idStr);
            if (repository.delete(id)) {
                ctx.status(HttpStatus.OK).json(java.util.Map.of("message", "Deleted"));
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", "Task not found"));
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", "Error deleting task"));
        }
    }
}
