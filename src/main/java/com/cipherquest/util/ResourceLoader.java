package com.cipherquest.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class ResourceLoader {

    private ResourceLoader() {}

    public static Properties loadProperties(String path) {
        Properties properties = new Properties();
        try (InputStream stream = ResourceLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load properties from: " + path, e);
        }
    }

    public static String loadString(String path) {
        try (InputStream stream = ResourceLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load text resource from: " + path, e);
        }
    }
}
