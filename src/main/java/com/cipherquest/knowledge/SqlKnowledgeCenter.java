package com.cipherquest.knowledge;

import com.cipherquest.levels.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SqlKnowledgeCenter {

    private final List<SqlTopic> topics;
    private final Map<String, SqlTopic> topicsById;
    private final List<String> categories;

    public SqlKnowledgeCenter() {
        List<SqlTopic> loaded = loadTopicsFromResource();
        this.topics = List.copyOf(loaded);

        Map<String, SqlTopic> byId = new LinkedHashMap<>();
        Set<String> categorySet = new LinkedHashSet<>();
        for (SqlTopic topic : loaded) {
            byId.put(topic.getId().toUpperCase(Locale.ROOT), topic);
            categorySet.add(topic.getCategory());
        }
        this.topicsById = Collections.unmodifiableMap(byId);
        this.categories = List.copyOf(categorySet);
    }

    public List<SqlTopic> getAllTopics() {
        return topics;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<SqlTopic> getTopicsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String target = category.trim();
        List<SqlTopic> result = new ArrayList<>();
        for (SqlTopic topic : topics) {
            if (topic.getCategory().equalsIgnoreCase(target)) {
                result.add(topic);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public Optional<SqlTopic> getTopicById(String topicId) {
        if (topicId == null || topicId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(topicsById.get(topicId.trim().toUpperCase(Locale.ROOT)));
    }

    public List<SqlTopic> findTopicsForConcepts(Collection<String> concepts) {
        if (concepts == null || concepts.isEmpty()) {
            return Collections.emptyList();
        }
        List<SqlTopic> matched = new ArrayList<>();
        for (SqlTopic topic : topics) {
            for (String concept : concepts) {
                if (topic.matchesConcept(concept)) {
                    matched.add(topic);
                    break;
                }
            }
        }
        return Collections.unmodifiableList(matched);
    }

    public List<SqlTopic> findTopicsForLevel(Level level) {
        if (level == null) {
            return Collections.emptyList();
        }
        return findTopicsForConcepts(level.getSqlConcepts());
    }

    private static List<SqlTopic> loadTopicsFromResource() {
        Properties properties = new Properties();
        try (InputStream stream = SqlKnowledgeCenter.class.getResourceAsStream("/data/knowledge/sql-topics.properties")) {
            if (stream == null) {
                throw new IllegalStateException("Missing SQL topics resource: /data/knowledge/sql-topics.properties");
            }
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL topics", e);
        }

        List<SqlTopic> result = new ArrayList<>();
        for (int i = 1; properties.containsKey("topic." + i + ".id"); i++) {
            String prefix = "topic." + i + ".";
            String id = properties.getProperty(prefix + "id");
            String category = properties.getProperty(prefix + "category");
            String title = properties.getProperty(prefix + "title");
            String conceptsStr = properties.getProperty(prefix + "concepts", "");
            List<String> concepts = Arrays.stream(conceptsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            String overview = properties.getProperty(prefix + "overview");
            String syntax = properties.getProperty(prefix + "syntax");
            String explanation = properties.getProperty(prefix + "explanation");
            String example = properties.getProperty(prefix + "example");
            String expectedResult = properties.getProperty(prefix + "expected_result");
            String mistakes = properties.getProperty(prefix + "mistakes");

            result.add(new SqlTopic(
                    id, category, title, concepts, overview, syntax, explanation, example, expectedResult, mistakes
            ));
        }
        return result;
    }
}
