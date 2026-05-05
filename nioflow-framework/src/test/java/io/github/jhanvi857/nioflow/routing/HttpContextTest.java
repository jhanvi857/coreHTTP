package io.github.jhanvi857.nioflow.routing;

import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.exception.UnsupportedMediaTypeException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HttpContextTest {

    @Test
    public void pathParamAsLong_variousCases() {
        HttpRequest req = mock(HttpRequest.class);
        HttpContext ctx = new HttpContext(req);
        
        // Missing
        assertThrows(IllegalArgumentException.class, () -> ctx.pathParamAsLong("id"));
        
        // Blank
        ctx.addPathParam("id", " ");
        assertThrows(IllegalArgumentException.class, () -> ctx.pathParamAsLong("id"));
        
        // Non-numeric
        ctx.addPathParam("id", "abc");
        assertThrows(IllegalArgumentException.class, () -> ctx.pathParamAsLong("id"));
        
        // Valid
        ctx.addPathParam("id", "123");
        assertEquals(123L, ctx.pathParamAsLong("id"));
    }

    @Test
    public void pathParamAsInt_variousCases() {
        HttpRequest req = mock(HttpRequest.class);
        HttpContext ctx = new HttpContext(req);
        
        // Missing
        assertThrows(IllegalArgumentException.class, () -> ctx.pathParamAsInt("age"));
        
        // Valid
        ctx.addPathParam("age", "25");
        assertEquals(25, ctx.pathParamAsInt("age"));
        
        // Non-numeric
        ctx.addPathParam("age", "old");
        assertThrows(IllegalArgumentException.class, () -> ctx.pathParamAsInt("age"));
    }

    @Test
    public void body_variousCases() {
        HttpRequest req = mock(HttpRequest.class);
        HttpContext ctx = new HttpContext(req);
        
        // Null body
        when(req.getBodyAsString()).thenReturn(null);
        assertNull(ctx.body(Map.class));
        
        // Empty body
        when(req.getBodyAsString()).thenReturn("");
        assertNull(ctx.body(Map.class));
        
        // Missing content-type
        when(req.getBodyAsString()).thenReturn("{}");
        when(req.getHeaders()).thenReturn(Map.of());
        assertThrows(UnsupportedMediaTypeException.class, () -> ctx.body(Map.class));
        
        // Wrong content-type
        when(req.getHeaders()).thenReturn(Map.of("Content-Type", "text/plain"));
        assertThrows(UnsupportedMediaTypeException.class, () -> ctx.body(Map.class));
        
        // Valid JSON
        when(req.getHeaders()).thenReturn(Map.of("Content-Type", "application/json"));
        Map result = ctx.body(Map.class);
        assertNotNull(result);
    }

    @Test
    public void status_preservesHeaders() {
        HttpRequest req = mock(HttpRequest.class);
        HttpContext ctx = new HttpContext(req);
        ctx.header("X-Test", "value");
        ctx.header("Content-Length", "100");
        
        ctx.status(HttpStatus.CREATED);
        
        assertEquals(201, ctx.getResponse().getStatus().getCode());
        assertEquals("value", ctx.getResponse().getHeadersMap().get("X-Test"));
        // Content-Length is auto-calculated by HttpResponse constructor (0 for empty body)
        assertEquals("0", ctx.getResponse().getHeadersMap().get("Content-Length"));
    }

    @Test
    public void send_preservesHeaders() {
        HttpRequest req = mock(HttpRequest.class);
        HttpContext ctx = new HttpContext(req);
        ctx.header("X-Test", "value");
        
        ctx.send("hello");
        
        assertEquals("value", ctx.getResponse().getHeadersMap().get("X-Test"));
        assertEquals("text/plain; charset=UTF-8", ctx.getResponse().getHeadersMap().get("Content-Type"));
    }

    @Test
    public void json_preservesHeaders() {
        HttpRequest req = mock(HttpRequest.class);
        HttpContext ctx = new HttpContext(req);
        ctx.header("X-Test", "value");
        
        ctx.json(Map.of("foo", "bar"));
        
        assertEquals("value", ctx.getResponse().getHeadersMap().get("X-Test"));
        assertEquals("application/json; charset=UTF-8", ctx.getResponse().getHeadersMap().get("Content-Type"));
    }

    @Test
    public void routePattern_defaultsToPath() {
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/foo");
        HttpContext ctx = new HttpContext(req);
        
        assertEquals("/foo", ctx.routePattern());
        
        ctx.setRoutePattern("/bar/:id");
        assertEquals("/bar/:id", ctx.routePattern());
    }

    @Test
    public void fork_copiesState() {
        HttpRequest req = mock(HttpRequest.class);
        HttpContext ctx = new HttpContext(req);
        ctx.addPathParam("id", "1");
        ctx.setRoutePattern("/user/:id");
        
        HttpContext forked = ctx.fork();
        assertEquals("1", forked.pathParam("id"));
        assertEquals("/user/:id", forked.routePattern());
    }
}
