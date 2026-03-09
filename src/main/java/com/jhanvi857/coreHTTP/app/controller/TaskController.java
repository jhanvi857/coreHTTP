package com.jhanvi857.coreHTTP.app.controller;

import com.jhanvi857.coreHTTP.app.model.Task;
import com.jhanvi857.coreHTTP.app.repository.TaskRepository;
import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;
import com.jhanvi857.coreHTTP.util.JsonUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TaskController {
    private final TaskRepository repository = new TaskRepository();

    public HttpResponse list(HttpRequest request) throws IOException {
        try {
            List<Task> tasks = repository.findAll();
            HttpResponse response = new HttpResponse(HttpStatus.OK, JsonUtils.toJson(tasks));
            response.addHeader("Content-Type", "application/json");
            return response;
        } catch (SQLException e) {
            HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Database error\"}");
            response.addHeader("Content-Type", "application/json");
            return response;
        }
    }

    public HttpResponse create(HttpRequest request) throws IOException {
        Task task = JsonUtils.fromJson(request.getBodyAsString(), Task.class);
        if (task == null || task.getTitle() == null) {
            HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST, "{\"error\": \"Title is required\"}");
            response.addHeader("Content-Type", "application/json");
            return response;
        }
        try {
            Task saved = repository.save(task);
            HttpResponse response = new HttpResponse(HttpStatus.CREATED, JsonUtils.toJson(saved));
            response.addHeader("Content-Type", "application/json");
            return response;
        } catch (SQLException e) {
            HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Database error\"}");
            response.addHeader("Content-Type", "application/json");
            return response;
        }
    }

    public HttpResponse get(HttpRequest request) throws IOException {
        String path = request.getPath();
        String idStr = path.substring(path.lastIndexOf("/") + 1);
        try {
            Long id = Long.parseLong(idStr);
            Optional<Task> task = repository.findById(id);
            if (task.isPresent()) {
                HttpResponse response = new HttpResponse(HttpStatus.OK, JsonUtils.toJson(task.get()));
                response.addHeader("Content-Type", "application/json");
                return response;
            } else {
                HttpResponse response = new HttpResponse(HttpStatus.NOT_FOUND, "{\"error\": \"Task not found\"}");
                response.addHeader("Content-Type", "application/json");
                return response;
            }
        } catch (NumberFormatException e) {
            HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST, "{\"error\": \"Invalid ID format\"}");
            response.addHeader("Content-Type", "application/json");
            return response;
        } catch (SQLException e) {
            HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Database error\"}");
            response.addHeader("Content-Type", "application/json");
            return response;
        }
    }

    public HttpResponse delete(HttpRequest request) throws IOException {
        String path = request.getPath();
        String idStr = path.substring(path.lastIndexOf("/") + 1);
        try {
            Long id = Long.parseLong(idStr);
            if (repository.delete(id)) {
                HttpResponse response = new HttpResponse(HttpStatus.OK, "{\"message\": \"Deleted\"}");
                response.addHeader("Content-Type", "application/json");
                return response;
            } else {
                HttpResponse response = new HttpResponse(HttpStatus.NOT_FOUND, "{\"error\": \"Task not found\"}");
                response.addHeader("Content-Type", "application/json");
                return response;
            }
        } catch (Exception e) {
            HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Error deleting task\"}");
            response.addHeader("Content-Type", "application/json");
            return response;
        }
    }
}
