package io.github.jhanvi857.nioflow.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized, hardened Jackson ObjectMapper configuration.
 *
 * <h3>Security measures</h3>
 * <ul>
 *   <li><b>Default typing disabled</b> — blocks the entire CVE-2017-7525
 *       gadget-chain class of deserialization attacks. Polymorphic types
 *       must use explicit {@code @JsonSubTypes} annotations.</li>
 *   <li><b>Unknown properties ignored</b> — prevents unexpected payloads from
 *       causing deserialization failures that leak internal model info.</li>
 * </ul>
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper mapper;

    static {
        mapper = JsonMapper.builder()
                // ── Security: explicitly disable default typing ──
                // enableDefaultTyping() would allow arbitrary class instantiation
                // via @class/@type JSON fields. This is the root cause of
                // CVE-2017-7525 and dozens of follow-on gadget-chain CVEs.
                .deactivateDefaultTyping()
                .build();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Failed to deserialize JSON to object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Returns the shared mapper instance for advanced usage.
     * Do NOT call enableDefaultTyping() on this instance.
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }
}
