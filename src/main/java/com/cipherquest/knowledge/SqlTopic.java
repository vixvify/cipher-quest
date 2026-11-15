package com.cipherquest.knowledge;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SqlTopic {

    private final String id;
    private final String category;
    private final String title;
    private final List<String> relatedConcepts;
    private final String conceptOverview;
    private final String syntax;
    private final String explanation;
    private final String exampleQuery;
    private final String expectedResult;
    private final String commonMistakes;

    public SqlTopic(
            String id,
            String category,
            String title,
            Collection<String> relatedConcepts,
            String conceptOverview,
            String syntax,
            String explanation,
            String exampleQuery,
            String expectedResult,
            String commonMistakes
    ) {
        this.id = requireText(id, "Topic id");
        this.category = requireText(category, "Topic category");
        this.title = requireText(title, "Topic title");
        this.relatedConcepts = relatedConcepts != null
                ? List.copyOf(relatedConcepts)
                : Collections.emptyList();
        this.conceptOverview = requireText(conceptOverview, "conceptOverview");
        this.syntax = requireText(syntax, "syntax");
        this.explanation = requireText(explanation, "explanation");
        this.exampleQuery = requireText(exampleQuery, "exampleQuery");
        this.expectedResult = requireText(expectedResult, "expectedResult");
        this.commonMistakes = requireText(commonMistakes, "commonMistakes");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value;
    }

    public boolean matchesConcept(String concept) {
        String normalizedTarget = normalizeConcept(concept);
        if (normalizedTarget.isEmpty()) {
            return false;
        }
        for (String tag : relatedConcepts) {
            if (normalizeConcept(tag).equals(normalizedTarget)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeConcept(String concept) {
        if (concept == null) {
            return "";
        }
        return concept.trim().replace("()", "").toLowerCase(Locale.ROOT);
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getRelatedConcepts() {
        return relatedConcepts;
    }

    public String getConceptOverview() {
        return conceptOverview;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getExampleQuery() {
        return exampleQuery;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public String getCommonMistakes() {
        return commonMistakes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SqlTopic other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SqlTopic{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
