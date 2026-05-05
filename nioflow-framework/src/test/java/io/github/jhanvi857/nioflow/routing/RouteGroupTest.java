package io.github.jhanvi857.nioflow.routing;

import io.github.jhanvi857.nioflow.middleware.CircuitBreakerMiddleware;
import io.github.jhanvi857.nioflow.middleware.Middleware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouteGroupTest {
    private Router mockRouter;
    private RouteGroup group;
    private RouteHandler handler;

    @BeforeEach
    void setUp() {
        mockRouter = mock(Router.class);
        group = new RouteGroup("/api", mockRouter);
        handler = (ctx) -> {};
        
        when(mockRouter.registerWithMiddleware(anyString(), anyString(), any(), anyList()))
            .thenReturn(mock(Route.class));
    }

    @Test
    void group_middleware_appliedToRoutesInGroup() {
        Middleware m1 = mock(Middleware.class);
        group.use(m1);
        group.get("/users", handler);

        ArgumentCaptor<List<Middleware>> middlewareCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockRouter).registerWithMiddleware(eq("GET"), eq("/api/users"), eq(handler), middlewareCaptor.capture());
        
        assertTrue(middlewareCaptor.getValue().contains(m1));
    }

    @Test
    void group_circuitBreakerMiddleware_setsGroupKey() {
        CircuitBreakerMiddleware cb = mock(CircuitBreakerMiddleware.class);
        group.use(cb);
        
        verify(cb).groupKey("/api");
    }

    @Test
    void combinePaths_handlesTrailingSlashInPrefix() {
        RouteGroup g2 = new RouteGroup("/api/", mockRouter);
        g2.get("/users", handler);
        verify(mockRouter).registerWithMiddleware(anyString(), eq("/api/users"), any(), any());
    }

    @Test
    void combinePaths_handlesNoSlashes() {
        RouteGroup g2 = new RouteGroup("api", mockRouter);
        g2.get("users", handler);
        verify(mockRouter).registerWithMiddleware(anyString(), eq("api/users"), any(), any());
    }

    @Test
    void combinePaths_handlesBothSlashes() {
        RouteGroup g2 = new RouteGroup("/api/", mockRouter);
        g2.get("/users", handler);
        verify(mockRouter).registerWithMiddleware(anyString(), eq("/api/users"), any(), any());
    }

    @Test
    void post_put_delete_workCorrectly() {
        group.post("/users", handler);
        verify(mockRouter).registerWithMiddleware(eq("POST"), eq("/api/users"), any(), any());

        group.put("/users", handler);
        verify(mockRouter).registerWithMiddleware(eq("PUT"), eq("/api/users"), any(), any());

        group.delete("/users", handler);
        verify(mockRouter).registerWithMiddleware(eq("DELETE"), eq("/api/users"), any(), any());
    }
}
