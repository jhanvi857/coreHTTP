package io.github.jhanvi857.taskplanner.controller;

import io.github.jhanvi857.taskplanner.model.Task;
import io.github.jhanvi857.taskplanner.repository.TaskRepository;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;

import java.util.List;
import java.util.Optional;

public class TaskController {
    private final TaskRepository repository = new TaskRepository();

    public void list(HttpContext ctx) {
        try {
            List<Task> tasks = repository.findAll().join();
            ctx.status(HttpStatus.OK).json(tasks);
        } catch (java.util.concurrent.CompletionException e) {
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
            Task saved = repository.save(task).join();
            ctx.status(HttpStatus.CREATED).json(saved);
        } catch (java.util.concurrent.CompletionException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", "Database error"));
        }
    }

    public void get(HttpContext ctx) {
        try {
            long id = ctx.pathParamAsLong("id");
            Optional<Task> task = repository.findById(id).join();
            if (task.isPresent()) {
                ctx.status(HttpStatus.OK).json(task.get());
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", "Task not found"));
            }
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", e.getMessage()));
        } catch (java.util.concurrent.CompletionException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", "Database error"));
        }
    }

    public void delete(HttpContext ctx) {
        try {
            long id = ctx.pathParamAsLong("id");
            if (repository.delete(id).join()) {
                ctx.status(HttpStatus.OK).json(java.util.Map.of("message", "Deleted"));
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(java.util.Map.of("error", "Task not found"));
            }
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", "Error deleting task"));
        }
    }
}
