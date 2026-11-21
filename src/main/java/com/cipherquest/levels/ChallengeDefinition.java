package com.cipherquest.levels;

import com.cipherquest.util.ResourceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ChallengeDefinition {
    private final Properties values;
    private final String path;

    private ChallengeDefinition(String name) {
        this.path = "/data/challenges/" + name + ".properties";
        this.values = ResourceLoader.loadProperties(path);
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
