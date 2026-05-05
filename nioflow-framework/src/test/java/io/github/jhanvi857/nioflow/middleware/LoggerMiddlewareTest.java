package io.github.jhanvi857.nioflow.middleware;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoggerMiddlewareTest {
    private HttpContext ctx;
    private HttpResponse res;
    private RouteHandler next;
    private AtomicBoolean nextCalled;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        ctx = mock(HttpContext.class);
        res = mock(HttpResponse.class);
        nextCalled = new AtomicBoolean(false);
        next = (c) -> nextCalled.set(true);

        when(ctx.method()).thenReturn("GET");
        when(ctx.path()).thenReturn("/test");
        when(ctx.getResponse()).thenReturn(res);
        when(res.getStatus()).thenReturn(HttpStatus.OK);

        logger = (Logger) LoggerFactory.getLogger(LoggerMiddleware.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void handle_logsHTTPMethod() throws Exception {
        LoggerMiddleware middleware = new LoggerMiddleware(false);
        middleware.process(ctx, next);
        
        String log = listAppender.list.get(0).getFormattedMessage();
        assertTrue(log.contains("GET"));
    }

    @Test
    void handle_logsPath() throws Exception {
        LoggerMiddleware middleware = new LoggerMiddleware(false);
        middleware.process(ctx, next);
        
        String log = listAppender.list.get(0).getFormattedMessage();
        assertTrue(log.contains("/test"));
    }

    @Test
    void handle_logsStatusCode() throws Exception {
        LoggerMiddleware middleware = new LoggerMiddleware(false);
        middleware.process(ctx, next);
        
        String log = listAppender.list.get(0).getFormattedMessage();
        assertTrue(log.contains("200"));
    }

    @Test
    void handle_alwaysCallsNext() throws Exception {
        LoggerMiddleware middleware = new LoggerMiddleware(false);
        middleware.process(ctx, next);
        assertTrue(nextCalled.get());
    }

    @Test
    void handle_queryString_present_logged() throws Exception {
        // Implementation uses ctx.path(), which usually includes query string in this framework
        when(ctx.path()).thenReturn("/search?q=test");
        LoggerMiddleware middleware = new LoggerMiddleware(false);
        middleware.process(ctx, next);
        
        String log = listAppender.list.get(0).getFormattedMessage();
        assertTrue(log.contains("q=test"));
    }

    @Test
    void handle_jsonMode_enabled_outputIsValidJson() throws Exception {
        LoggerMiddleware middleware = new LoggerMiddleware(true);
        middleware.process(ctx, next);
        
        String log = listAppender.list.get(0).getFormattedMessage();
        assertTrue(log.startsWith("{") && log.endsWith("}"));
        assertTrue(log.contains("\"method\":\"GET\""));
    }

    @Test
    void handle_nextThrows_exceptionBehaviourMatches() throws Exception {
        RouteHandler errorNext = (c) -> { throw new RuntimeException("Logged Error"); };
        LoggerMiddleware middleware = new LoggerMiddleware(false);
        
        assertThrows(RuntimeException.class, () -> middleware.process(ctx, errorNext));
        
        String log = listAppender.list.get(0).getFormattedMessage();
        assertTrue(log.contains("Logged Error"));
        assertEquals(Level.ERROR, listAppender.list.get(0).getLevel());
    }

    @Test
    void handle_jsonMode_nextThrows() throws Exception {
        RouteHandler errorNext = (c) -> { throw new RuntimeException("JSON Error"); };
        LoggerMiddleware middleware = new LoggerMiddleware(true);
        
        assertThrows(RuntimeException.class, () -> middleware.process(ctx, errorNext));
        
        String log = listAppender.list.get(0).getFormattedMessage();
        assertTrue(log.contains("\"error\":\"JSON Error\""));
        assertTrue(log.contains("\"statusCode\":500"));
    }
}
