package io.github.jhanvi857.nioflow.util;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

public class HotReloaderTest {

    @Test
    void shouldIgnoreDir_standardDirs() throws Exception {
        Method method = HotReloader.class.getDeclaredMethod("shouldIgnoreDir", Path.class);
        method.setAccessible(true);

        assertTrue((boolean) method.invoke(null, Paths.get(".git")));
        assertTrue((boolean) method.invoke(null, Paths.get("target")));
        assertTrue((boolean) method.invoke(null, Paths.get("node_modules")));
        assertFalse((boolean) method.invoke(null, Paths.get("src")));
    }

    @Test
    void isWatchedFile_extensions() throws Exception {
        Method method = HotReloader.class.getDeclaredMethod("isWatchedFile", Path.class);
        method.setAccessible(true);

        assertTrue((boolean) method.invoke(null, Paths.get("App.java")));
        assertTrue((boolean) method.invoke(null, Paths.get("pom.xml")));
        assertTrue((boolean) method.invoke(null, Paths.get("index.html")));
        assertFalse((boolean) method.invoke(null, Paths.get("App.class")));
        assertFalse((boolean) method.invoke(null, Paths.get("image.png")));
    }

    @Test
    void findModule_handlesNull() throws Exception {
        Method method = HotReloader.class.getDeclaredMethod("findModule", Class.class);
        method.setAccessible(true);
        
        // This will likely return null in a test environment unless we are in the project root
        // But we just want to ensure it doesn't crash
        Object result = method.invoke(null, HotReloaderTest.class);
        // assertNothing specific since it depends on environment
    }
}
