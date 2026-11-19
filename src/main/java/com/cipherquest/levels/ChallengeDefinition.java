package com.cipherquest.levels;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ChallengeDefinition {
    private final Properties values = new Properties();
    private final String path;

    private ChallengeDefinition(String name) {
        path = "/data/challenges/" + name + ".properties";
        try (InputStream stream = ChallengeDefinition.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing challenge resource " + path);
            }
            values.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read challenge resource " + path, e);
        }
    }

    public static ChallengeDefinition load(int checkpoint) {
        return new ChallengeDefinition("checkpoint-" + checkpoint);
    }

    public static ChallengeDefinition loadDefaultDatabase() {
        return new ChallengeDefinition("database-default");
    }

    public String required(String key) {
        String value = optional(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing challenge field " + key + " in " + path);
        }
        return value;
    }

    public String optional(String key) {
        return values.getProperty(key);
    }

    public List<String> list(String key) {
        List<String> entries = new ArrayList<>();
        for (int index = 1; values.containsKey(key + "." + index); index++) {
            entries.add(required(key + "." + index));
        }
        return List.copyOf(entries);
    }
}
