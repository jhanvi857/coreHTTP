package io.github.jhanvi857.nioflow.util;

import io.github.jhanvi857.nioflow.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HotReloader provides nodemon-like functionality for NioFlow applications.
 * It watches the project directory for changes, recompiles the project (if
 * Maven is used),
 * and restarts the application in a child process.
 */
@SuppressWarnings("unused")
public class HotReloader {
    private static final Logger logger = LoggerFactory.getLogger(HotReloader.class);
    private static Process childProcess;
    private static final String CHILD_MARKER = "NIOFLOW_WATCH_CHILD";
    private static final AtomicBoolean restarting = new AtomicBoolean(false);

    /**
     * Initializes hot reload if NIOFLOW_WATCH is enabled and not already in a child
     * process.
     * This method should be called at the very beginning of the main method.
     *
     * @param mainClass The main class of the application.
     * @param args      The command line arguments.
     */
    public static void setup(Class<?> mainClass, String[] args) {
        if (!Env.getAsBoolean("NIOFLOW_WATCH", false)) {
            return;
        }

        if (System.getProperty(CHILD_MARKER) != null) {
            return;
        }

        logger.info("=================================================");
        logger.info("NioFlow Hot Reload (Watch Mode) Active");
        logger.info("Watching for changes in: " + Paths.get("").toAbsolutePath());
        logger.info("=================================================");

        // Registering shutdown hook to kill child process when parent dies
        Runtime.getRuntime().addShutdownHook(new Thread(HotReloader::stopChild));

        // Starting the watcher thread
        startWatcher(mainClass, args);

        // Keep the parent process alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            stopChild();
            System.exit(0);
        }
    }

    private static void startWatcher(Class<?> mainClass, String[] args) {
        Thread watcherThread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path root = Paths.get("").toAbsolutePath();
                Map<WatchKey, Path> keys = new HashMap<>();

                registerRecursive(root, watchService, keys);

                // Initial start
                startChild(mainClass, args);

                while (true) {
                    WatchKey key = watchService.take();
                    Path dir = keys.get(key);
                    if (dir == null)
                        continue;

                    boolean changeDetected = false;
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW)
                            continue;

                        Path name = (Path) event.context();
                        Path child = dir.resolve(name);

                        if (isWatchedFile(child)) {
                            changeDetected = true;
                        }
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(child)) {
                            registerRecursive(child, watchService, keys);
                        }
                    }

                    if (changeDetected && !restarting.get()) {
                        Thread.sleep(800);
                        key.pollEvents();
                        logger.info("Change detected. Restarting application...");
                        restartChild(mainClass, args);
                    }

                    if (!key.reset()) {
                        keys.remove(key);
                        if (keys.isEmpty())
                            break;
                    }
                }
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    logger.error("HotReloader encountered an error: ", e);
                }
            }
        });
        watcherThread.setName("NioFlow-Watcher");
        watcherThread.setDaemon(false);
        watcherThread.start();
    }

    private static void registerRecursive(Path root, WatchService watchService, Map<WatchKey, Path> keys)
            throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (shouldIgnoreDir(dir))
                    return FileVisitResult.SKIP_SUBTREE;
                WatchKey key = dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean shouldIgnoreDir(Path dir) {
        String name = dir.getFileName().toString().toLowerCase();
        return name.startsWith(".") || name.equals("target") || name.equals("out")
                || name.equals("bin") || name.equals("node_modules") || name.equals(".maven")
                || name.equals(".mvn") || name.equals(".git");
    }

    private static boolean isWatchedFile(Path file) {
        if (Files.isDirectory(file))
            return false;
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".xml") || name.endsWith(".properties")
                || name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".json")
                || name.endsWith(".sql") || name.endsWith(".html") || name.endsWith(".css");
    }

    private static void startChild(Class<?> mainClass, String[] args) {
        if (restarting.getAndSet(true))
            return;

        try {
            List<String> command = new ArrayList<>();
            Path cwd = Paths.get("").toAbsolutePath();
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                if (Files.exists(cwd.resolve("mvn.ps1"))) {
                    command.add("powershell.exe");
                    command.add("-ExecutionPolicy");
                    command.add("Bypass");
                    command.add("-File");
                    command.add("./mvn.ps1");
                } else if (Files.exists(cwd.resolve("mvnw.cmd"))) {
                    command.add("cmd");
                    command.add("/c");
                    command.add("mvnw.cmd");
                } else {
                    command.add("cmd");
                    command.add("/c");
                    command.add("mvn");
                }
            } else {
                if (Files.exists(cwd.resolve("mvnw"))) {
                    command.add("./mvnw");
                } else {
                    command.add("mvn");
                }
            }

            String module = findModule(mainClass);

            // 1. Run compile step synchronously first to ensure all modules are ready
            if (!runBuildCommand(module)) {
                logger.error("Build failed. Aborting restart.");
                restarting.set(false);
                return;
            }

            if (module != null) {
                command.add("-pl");
                command.add(module);
            }

            command.add("exec:java");
            command.add("-Dexec.mainClass=" + mainClass.getName());
            command.add("-D" + CHILD_MARKER + "=true");

            // Forwarding important system properties
            System.getProperties().forEach((k, v) -> {
                String key = k.toString();
                if (key.startsWith("nioflow") || key.equals("PORT") || key.equals("JWT_SECRET")) {
                    command.add("-D" + key + "=" + v);
                }
            });

            if (args.length > 0) {
                // Properly quote args for exec:java
                command.add("-Dexec.args=" + String.join(" ", args));
            }

            System.out.println("Starting child process via Maven...");
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            childProcess = pb.start();
        } catch (Exception e) {
            logger.error("Failed to start child process: ", e);
        } finally {
            restarting.set(false);
        }
    }

    private static String findModule(Class<?> mainClass) {
        Path cwd = Paths.get("").toAbsolutePath();
        String relativePath = "src/main/java/" + mainClass.getName().replace('.', '/') + ".java";
        logger.debug("Searching for module containing: {}", relativePath);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cwd)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry) && Files.exists(entry.resolve("pom.xml"))) {
                    Path sourceFile = entry.resolve(relativePath);
                    logger.debug("Checking: {}", sourceFile);
                    if (Files.exists(sourceFile)) {
                        logger.info("Found module: {}", entry.getFileName());
                        return entry.getFileName().toString();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error during module detection: ", e);
        }
        logger.warn("No module found for {}", mainClass.getName());
        return null;
    }

    private static boolean runBuildCommand(String module) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            List<String> command = new ArrayList<>();
            Path cwd = Paths.get("").toAbsolutePath();

            if (os.contains("win")) {
                if (Files.exists(cwd.resolve("mvn.ps1"))) {
                    command.add("powershell.exe");
                    command.add("-ExecutionPolicy");
                    command.add("Bypass");
                    command.add("-File");
                    command.add("./mvn.ps1");
                } else if (Files.exists(cwd.resolve("mvnw.cmd"))) {
                    command.add("cmd");
                    command.add("/c");
                    command.add("mvnw.cmd");
                } else {
                    command.add("cmd");
                    command.add("/c");
                    command.add("mvn");
                }
            } else {
                if (Files.exists(cwd.resolve("mvnw"))) {
                    command.add("./mvnw");
                } else {
                    command.add("mvn");
                }
            }

            if (module != null) {
                command.add("-pl");
                command.add(module);
                command.add("-am");
            }
            command.add("compile");

            System.out.println("Running build command: " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            Process p = pb.start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            logger.warn("Could not execute build command: {}. Ensure Maven is in your PATH.", e.getMessage());
            return true;
        }
    }

    private static synchronized void stopChild() {
        if (childProcess != null && childProcess.isAlive()) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                try {
                    // On Windows, killing the parent (Maven/Powershell) might leave the child
                    // (Java) alive.
                    // taskkill /T /F kills the whole tree.
                    long pid = childProcess.pid();
                    new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pid)).start().waitFor();
                } catch (Exception e) {
                    childProcess.destroyForcibly();
                }
            } else {
                childProcess.destroy();
                try {
                    if (!childProcess.waitFor(5, TimeUnit.SECONDS)) {
                        childProcess.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    childProcess.destroyForcibly();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static synchronized void restartChild(Class<?> mainClass, String[] args) {
        stopChild();
        startChild(mainClass, args);
    }
}
