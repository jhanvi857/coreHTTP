package io.github.jhanvi857.nioflow.plugin;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.NioFlowPlugin;
import io.github.jhanvi857.nioflow.server.StaticFileHandler;

public class StaticFilesPlugin implements NioFlowPlugin {
    private final String directory;
    private final String mountPath;

    public StaticFilesPlugin(String directory) {
        this(directory, "/");
    }

    public StaticFilesPlugin(String directory, String mountPath) {
        this.directory = directory;
        this.mountPath = mountPath;
    }

    @Override
    public void onRegister(NioFlowApp app) {
        String pattern = mountPath.endsWith("/") ? mountPath + "*" : mountPath + "/*";
        app.get(pattern, new StaticFileHandler(directory, mountPath));
    }
}
