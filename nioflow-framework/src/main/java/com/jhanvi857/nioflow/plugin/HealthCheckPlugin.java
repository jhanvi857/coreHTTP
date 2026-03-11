package com.jhanvi857.nioflow.plugin;

import com.jhanvi857.nioflow.NioFlowApp;
import com.jhanvi857.nioflow.NioFlowPlugin;
import com.jhanvi857.nioflow.observability.HealthCheckHandler;

public class HealthCheckPlugin implements NioFlowPlugin {
    private final String path;

    public HealthCheckPlugin() {
        this("/_health");
    }

    public HealthCheckPlugin(String path) {
        this.path = path;
    }

    @Override
    public void onRegister(NioFlowApp app) {
        app.get(path, new HealthCheckHandler());
    }
}
