package com.cipherquest.clues;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Clue {

    private final String id;
    private final String title;
    private final ClueType type;
    private final String source;
    private final String summary;
    private final List<String> details;

    public Clue(String id, String title, ClueType type, String source, String summary, List<String> details) {
        this.id = Objects.requireNonNull(id, "Clue id cannot be null");
        this.title = Objects.requireNonNull(title, "Clue title cannot be null");
        this.type = Objects.requireNonNull(type, "Clue type cannot be null");
        this.source = source != null ? source : "";
        this.summary = summary != null ? summary : "";
        this.details = details != null ? List.copyOf(details) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ClueType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Clue clue)) return false;
        return Objects.equals(id, clue.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Clue{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                '}';
    }
}
