package com.cipherquest.util;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ResourceLoaderTest {

    @Test
    void testLoadPropertiesSuccess() {
        Properties props = ResourceLoader.loadProperties("/data/challenges/checkpoint-1.properties");
        assertNotNull(props);
        assertEquals("Operation Caesar", props.getProperty("title"));
    }

    @Test
    void testLoadPropertiesMissingThrows() {
        assertThrows(IllegalStateException.class, () -> ResourceLoader.loadProperties("/data/missing.properties"));
    }

    @Test
    void testLoadStringSuccess() {
        String seed = ResourceLoader.loadString("/data/database/seed.sql");
        assertNotNull(seed);
        assertTrue(seed.contains("CREATE TABLE locations"));
    }

    @Test
    void testLoadStringMissingThrows() {
        assertThrows(IllegalStateException.class, () -> ResourceLoader.loadString("/data/missing.sql"));
    }
}
