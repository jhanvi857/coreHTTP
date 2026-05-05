package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CsrfMiddlewareTest {
    private CsrfMiddleware middleware;
    private HttpContext ctx;
    private HttpRequest req;
    private HttpResponse res;
    private RouteHandler next;
    private AtomicBoolean nextCalled;

    @BeforeEach
    void setUp() {
        middleware = new CsrfMiddleware();
        req = mock(HttpRequest.class);
        res = mock(HttpResponse.class);
        ctx = mock(HttpContext.class);
        nextCalled = new AtomicBoolean(false);
        next = (c) -> nextCalled.set(true);

        when(ctx.getRequest()).thenReturn(req); // for completeness if used
        when(ctx.getResponse()).thenReturn(res); // for completeness if used
        when(ctx.getResponse()).thenReturn(res);
        when(ctx.status(anyInt())).thenReturn(ctx);
        when(ctx.status(any(HttpStatus.class))).thenReturn(ctx);
    }

    @Test
    void safeMethod_GET_skipsCheck_nextCalled() throws Exception {
        when(ctx.method()).thenReturn("GET");
        when(ctx.header("Cookie")).thenReturn(null);

        middleware.process(ctx, next);

        assertTrue(nextCalled.get());
        verify(ctx, times(1)).getResponse();
        verify(res, times(1)).addHeader(eq("Set-Cookie"), contains("CSRF-TOKEN="));
    }

    @Test
    void safeMethod_HEAD_skipsCheck_nextCalled() throws Exception {
        when(ctx.method()).thenReturn("HEAD");
        when(ctx.header("Cookie")).thenReturn("CSRF-TOKEN=existing");

        middleware.process(ctx, next);

        assertTrue(nextCalled.get());
        verify(res, never()).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void safeMethod_OPTIONS_skipsCheck_nextCalled() throws Exception {
        when(ctx.method()).thenReturn("OPTIONS");
        middleware.process(ctx, next);
        assertTrue(nextCalled.get());
    }

    @Test
    void unsafeMethod_POST_noToken_returns403_nextNotCalled() throws Exception {
        when(ctx.method()).thenReturn("POST");
        when(ctx.header("X-CSRF-TOKEN")).thenReturn(null);
        when(ctx.header("Cookie")).thenReturn(null);

        middleware.process(ctx, next);

        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
        verify(ctx).json(argThat(m -> m instanceof Map && ((Map) m).containsKey("error")));
    }

    @Test
    void unsafeMethod_PUT_noToken_returns403_nextNotCalled() throws Exception {
        when(ctx.method()).thenReturn("PUT");
        middleware.process(ctx, next);
        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
    }

    @Test
    void unsafeMethod_DELETE_noToken_returns403_nextNotCalled() throws Exception {
        when(ctx.method()).thenReturn("DELETE");
        middleware.process(ctx, next);
        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
    }

    @Test
    void unsafeMethod_PATCH_noToken_returns403_nextNotCalled() throws Exception {
        when(ctx.method()).thenReturn("PATCH");
        middleware.process(ctx, next);
        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
    }

    @Test
    void unsafeMethod_POST_validToken_passes_nextCalled() throws Exception {
        when(ctx.method()).thenReturn("POST");
        String token = "valid-token";
        when(ctx.header("X-CSRF-TOKEN")).thenReturn(token);
        when(ctx.header("Cookie")).thenReturn("CSRF-TOKEN=" + token);

        middleware.process(ctx, next);

        assertTrue(nextCalled.get());
        verify(ctx, never()).status(anyInt());
    }

    @Test
    void unsafeMethod_POST_mismatchedToken_returns403_nextNotCalled() throws Exception {
        when(ctx.method()).thenReturn("POST");
        when(ctx.header("X-CSRF-TOKEN")).thenReturn("token1");
        when(ctx.header("Cookie")).thenReturn("CSRF-TOKEN=token2");

        middleware.process(ctx, next);

        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
    }

    @Test
    void unsafeMethod_POST_emptyToken_returns403_nextNotCalled() throws Exception {
        when(ctx.method()).thenReturn("POST");
        when(ctx.header("X-CSRF-TOKEN")).thenReturn("");
        when(ctx.header("Cookie")).thenReturn("CSRF-TOKEN=");

        middleware.process(ctx, next);

        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
    }

    @Test
    void unsafeMethod_POST_nullToken_returns403_nextNotCalled() throws Exception {
        when(ctx.method()).thenReturn("POST");
        when(ctx.header("X-CSRF-TOKEN")).thenReturn(null);
        when(ctx.header("Cookie")).thenReturn("CSRF-TOKEN=token");

        middleware.process(ctx, next);

        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
    }

    @Test
    void doubleSubmit_cookieMatchesHeader_passes() throws Exception {
        when(ctx.method()).thenReturn("POST");
        when(ctx.header("X-CSRF-TOKEN")).thenReturn("abc");
        when(ctx.header("Cookie")).thenReturn("other=123; CSRF-TOKEN=abc; another=456");

        middleware.process(ctx, next);

        assertTrue(nextCalled.get());
    }

    @Test
    void doubleSubmit_cookieMismatchHeader_returns403() throws Exception {
        when(ctx.method()).thenReturn("POST");
        when(ctx.header("X-CSRF-TOKEN")).thenReturn("abc");
        when(ctx.header("Cookie")).thenReturn("CSRF-TOKEN=def");

        middleware.process(ctx, next);

        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.FORBIDDEN);
    }
}
