package com.jhanvi857.coreHTTP.observability;

import com.jhanvi857.coreHTTP.db.DatabaseManager;
import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;
import com.jhanvi857.coreHTTP.routing.RouteHandler;
import java.io.IOException;
import java.sql.Connection;

public class HealthCheckHandler implements RouteHandler {

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        long memoryUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;

        boolean dbEnabled = DatabaseManager.isEnabled();
        boolean dbOk = checkDatabase();

        String status = (!dbEnabled || dbOk) ? "UP" : "DEGRADED";
        HttpStatus httpStatus = (!dbEnabled || dbOk) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

        String dbStatus = dbEnabled ? (dbOk ? "OK" : "ERROR") : "DISABLED";

        String body = String.format(
                "{\"status\": \"%s\", \"database\": \"%s\", \"memory_used_mb\": %d}",
                status, dbStatus, memoryUsed);

        HttpResponse response = new HttpResponse(httpStatus, body);
        response.addHeader("Content-Type", "application/json");
        return response;
    }

    private boolean checkDatabase() {
        if (!DatabaseManager.isEnabled()) {
            return true;
        }
        try (Connection con = DatabaseManager.getConnection()) {
            return con.isValid(1);
        } catch (Exception e) {
            return false;
        }
    }
}
