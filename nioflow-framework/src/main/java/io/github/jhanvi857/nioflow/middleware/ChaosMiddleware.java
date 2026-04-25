package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.Env;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlled chaos injection middleware guarded by NIOFLOW_CHAOS_ENABLED.
 */
public class ChaosMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(ChaosMiddleware.class);

    private final List<ChaosAction> actions = new ArrayList<>();

    public ChaosMiddleware latency(int ms, double probability) {
        actions.add(new ChaosAction(ChaosType.LATENCY, Math.max(0, ms), clamp(probability)));
        return this;
    }

    public ChaosMiddleware error(int statusCode, double probability) {
        actions.add(new ChaosAction(ChaosType.ERROR, statusCode, clamp(probability)));
        return this;
    }

    public ChaosMiddleware drop(double probability) {
        actions.add(new ChaosAction(ChaosType.DROP, 0, clamp(probability)));
        return this;
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        if (!Env.getAsBoolean("NIOFLOW_CHAOS_ENABLED", false)) {
            next.handle(ctx);
            return;
        }

        for (ChaosAction action : actions) {
            if (!hit(action.probability)) {
                continue;
            }

            switch (action.type) {
                case LATENCY:
                    logger.warn("chaos=latency route={} path={} delayMs={}", ctx.routePattern(), ctx.path(), action.value);
                    Thread.sleep(action.value);
                    break;
                case ERROR:
                    logger.warn("chaos=error route={} path={} status={}", ctx.routePattern(), ctx.path(), action.value);
                    ctx.status(HttpStatus.fromCode(action.value)).json(java.util.Map.of("error", "Injected chaos fault"));
                    return;
                case DROP:
                    logger.warn("chaos=drop route={} path={}", ctx.routePattern(), ctx.path());
                    ctx.dropResponse();
                    return;
                default:
                    break;
            }
        }

        next.handle(ctx);
    }

    private static boolean hit(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private static double clamp(double probability) {
        if (probability < 0.0d) {
            return 0.0d;
        }
        if (probability > 1.0d) {
            return 1.0d;
        }
        return probability;
    }

    private enum ChaosType {
        LATENCY,
        ERROR,
        DROP
    }

    private static class ChaosAction {
        private final ChaosType type;
        private final int value;
        private final double probability;

        private ChaosAction(ChaosType type, int value, double probability) {
            this.type = type;
            this.value = value;
            this.probability = probability;
        }
    }
}
