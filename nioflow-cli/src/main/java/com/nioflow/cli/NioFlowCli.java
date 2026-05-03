package com.nioflow.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NioFlowCli {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String PREFIX = "[nioflow] ";

    public static void main(String[] args) {
        checkJavaVersion();

        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0];

        try {
            switch (command) {
                case "--version":
                case "-v":
                    System.out.println("nioflow-cli 1.2.0");
                    break;
                case "help":
                case "--help":
                case "-h":
                    printUsage();
                    break;
                case "new":
                    if (args.length < 2) {
                        error("Missing project name. Usage: nioflow new <project-name>");
                        return;
                    }
                    handleNew(args[1]);
                    break;
                case "run":
                    handleRun(false);
                    break;
                case "dev":
                    handleRun(true);
                    break;
                default:
                    error("Unknown command: " + command);
                    printUsage();
            }
        } catch (Exception e) {
            error("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void checkJavaVersion() {
        String version = System.getProperty("java.version");
        String major = version.split("[.\\-+]")[0];
        int majorVersion = Integer.parseInt(major);
        if (majorVersion < 17) {
            error("Java 17 or higher is required. Found: " + version);
            System.exit(1);
        }
    }

    private static void printUsage() {
        info("Usage:");
        info("  nioflow new <project-name>  - Scaffold a new project");
        info("  nioflow run                 - Run the current project");
        info("  nioflow dev                 - Run the current project with hot-reload enabled");
    }

    private static void info(String message) {
        System.out.println(ANSI_YELLOW + PREFIX + message + ANSI_RESET);
    }

    private static void success(String message) {
        System.out.println(ANSI_GREEN + PREFIX + message + ANSI_RESET);
    }

    private static void error(String message) {
        System.err.println(ANSI_RED + PREFIX + message + ANSI_RESET);
    }

    private static void handleNew(String projectName) throws IOException {
        File dir = new File(projectName);
        if (dir.exists()) {
            System.out.print(ANSI_YELLOW + PREFIX + "Directory already exists. Overwrite? (y/n): " + ANSI_RESET);
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("y") && !response.equals("yes")) {
                info("Aborted.");
                return;
            }
            if (!deleteDirectory(dir)) {
                error("Failed to delete existing directory. Check file permissions.");
                return;
            }
        }

        if (!dir.mkdirs()) {
            error("Failed to create directory: " + projectName);
            return;
        }

        // Generate files
        writeStringToFile(new File(dir, "pom.xml"), getPomTemplate(projectName));
        writeStringToFile(new File(dir, ".env.example"), getEnvTemplate());
        writeStringToFile(new File(dir, ".env"), getEnvTemplate());
        writeStringToFile(new File(dir, "nioflow.json"), getNioFlowJsonTemplate());
        writeStringToFile(new File(dir, ".gitignore"), getGitIgnoreTemplate());

        File javaDir = new File(dir, "src/main/java/com/example");
        javaDir.mkdirs();
        writeStringToFile(new File(javaDir, "App.java"), getAppTemplate());

        // Copy maven wrapper
        copyResource("/scaffold/mvnw", new File(dir, "mvnw"), true);
        copyResource("/scaffold/mvnw.cmd", new File(dir, "mvnw.cmd"), true);
        File wrapperDir = new File(dir, ".mvn/wrapper");
        wrapperDir.mkdirs();
        copyResource("/scaffold/.mvn/wrapper/maven-wrapper.properties",
                new File(wrapperDir, "maven-wrapper.properties"), false);

        success("Project " + projectName + " created successfully.");
        info("To get started:");
        info("  cd " + projectName);
        info("  Edit .env with your secrets before running");
        info("  nioflow dev");
    }

    private static void copyResource(String resourcePath, File dest, boolean executable) throws IOException {
        InputStream is = NioFlowCli.class.getResourceAsStream(resourcePath);
        if (is == null) {
            info("Warning: Could not find resource " + resourcePath
                    + " in CLI jar. Maven wrapper won't be fully set up.");
            return;
        }
        Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if (executable) {
            dest.setExecutable(true, false);
        }
    }

    private static void handleRun(boolean devMode) throws IOException, InterruptedException {
        File configFile = new File("nioflow.json");
        if (!configFile.exists()) {
            error("No nioflow.json found. Run nioflow new <name> first.");
            System.exit(1);
        }

        String content = new String(Files.readAllBytes(configFile.toPath()));
        String mainClass = extractJsonValue(content, "mainClass");

        if (mainClass == null || mainClass.isEmpty()) {
            error("Could not read mainClass from nioflow.json.");
            System.exit(1);
        }

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String mvnw = isWindows ? ".\\mvnw.cmd" : "./mvnw";

        File wrapperFile = new File(isWindows ? "mvnw.cmd" : "mvnw");
        if (!wrapperFile.exists()) {
            error("Maven wrapper not found in current directory. Cannot execute.");
            System.exit(1);
        }

        ProcessBuilder pb;
        if (devMode) {
            pb = new ProcessBuilder(mvnw, "clean", "compile", "exec:java", "-Dexec.mainClass=" + mainClass);
        } else {
            pb = new ProcessBuilder(mvnw, "compile", "exec:java", "-Dexec.mainClass=" + mainClass);
        }

        pb.inheritIO();
        pb.environment().put("NIOFLOW_WATCH", devMode ? "true" : "false");

        info("Starting " + (devMode ? "dev" : "run") + " mode...");
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            error("Process exited with code " + exitCode);
            System.exit(exitCode);
        }
    }

    private static String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static void writeStringToFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content);
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private static String getPomTemplate(String projectName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <groupId>com.example</groupId>\n" +
                "    <artifactId>" + projectName + "</artifactId>\n" +
                "    <version>1.0.0-SNAPSHOT</version>\n" +
                "\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "        <maven.compiler.target>17</maven.compiler.target>\n" +
                "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "    </properties>\n" +
                "\n" +
                "    <dependencies>\n" +
                "        <dependency>\n" +
                "            <groupId>io.github.jhanvi857</groupId>\n" +
                "            <artifactId>nioflow-framework</artifactId>\n" +
                "            <version>1.3.0</version>\n" +
                "        </dependency>\n" +
                "    </dependencies>\n" +
                "\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.codehaus.mojo</groupId>\n" +
                "                <artifactId>exec-maven-plugin</artifactId>\n" +
                "                <version>3.1.0</version>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>\n";
    }

    private static String getEnvTemplate() {
        return "JWT_SECRET=replace-this-with-a-32-plus-character-secret-key\n" +
                "PORT=8080\n" +
                "NIOFLOW_CHAOS_ENABLED=false\n" +
                "NIOFLOW_REPLAY_ENABLED=false\n" +
                "NIOFLOW_WATCH=false\n";
    }

    private static String getNioFlowJsonTemplate() {
        return "{\n" +
                "  \"mainClass\": \"com.example.App\",\n" +
                "  \"port\": 8080,\n" +
                "  \"nioflowVersion\": \"1.0.0\"\n" +
                "}\n";
    }

    private static String getGitIgnoreTemplate() {
        return ".env\n" +
                "target/\n" +
                "*.class\n";
    }

    private static String getAppTemplate() {
        return "package com.example;\n" +
                "\n" +
                "import io.github.jhanvi857.nioflow.NioFlowApp;\n" +
                "\n" +
                "public class App {\n" +
                "    public static void main(String[] args) {\n" +
                "        NioFlowApp.enableHotReload(App.class, args);\n" +
                "        NioFlowApp app = new NioFlowApp();\n" +
                "        \n" +
                "        app.get(\"/\", ctx -> {\n" +
                "            ctx.status(200).send(\"Hello World from NioFlow!\");\n" +
                "        });\n" +
                "        \n" +
                "        app.listen(8080);\n" +
                "    }\n" +
                "}\n";
    }
}
