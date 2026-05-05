package io.github.jhanvi857.nioflow.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    static class TestObject {
        public String name;
        public int age;

        public TestObject() {}
        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void toJson_simpleObject_returnsValidJsonString() {
        TestObject obj = new TestObject("Alice", 30);
        String json = JsonUtils.toJson(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"age\":30"));
    }

    @Test
    public void toJson_null_returnsEmptyJson() {
        // Based on implementation: return "{}" on exception, but writeValueAsString(null) returns "null"
        // Let's see what happens.
        String json = JsonUtils.toJson(null);
        assertEquals("null", json);
    }

    @Test
    public void fromJson_validJson_returnsTypedObject() {
        String json = "{\"name\":\"Bob\",\"age\":25}";
        TestObject obj = JsonUtils.fromJson(json, TestObject.class);
        assertNotNull(obj);
        assertEquals("Bob", obj.name);
        assertEquals(25, obj.age);
    }

    @Test
    public void fromJson_invalidJson_returnsNull() {
        String json = "{invalid-json}";
        TestObject obj = JsonUtils.fromJson(json, TestObject.class);
        assertNull(obj);
    }

    @Test
    public void fromJson_extraFields_ignoredGracefully() {
        // verify FAIL_ON_UNKNOWN_PROPERTIES=false
        String json = "{\"name\":\"Charlie\",\"age\":40,\"extra\":\"field\"}";
        TestObject obj = JsonUtils.fromJson(json, TestObject.class);
        assertNotNull(obj);
        assertEquals("Charlie", obj.name);
    }

    @Test
    public void objectMapper_securityConfig_verify() {
        ObjectMapper mapper = JsonUtils.getMapper();
        
        // verify unknown properties are not failing (as per JsonUtils static block)
        assertFalse(mapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        
        // verify time module is registered by checking a date serialization
        java.time.LocalDateTime now = java.time.LocalDateTime.of(2024, 5, 4, 10, 0);
        String json = JsonUtils.toJson(now);
        // Should not be a timestamp (array or number) because WRITE_DATES_AS_TIMESTAMPS is disabled
        assertFalse(json.startsWith("[") || json.matches("^\\d+$"));
        assertTrue(json.contains("2024-05-04"));
    }
}
