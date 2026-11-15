package com.cipherquest.knowledge;

import com.cipherquest.levels.database.DatabaseLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class SqlKnowledgeCenterTest {

    private SqlKnowledgeCenter knowledgeCenter;

    @BeforeEach
    void setUp() {
        knowledgeCenter = new SqlKnowledgeCenter();
    }

    @Test
    void testCoversAllEightGddCategoriesInLearningOrder() {
        List<String> expectedCategories = List.of(
                "พื้นฐานการดึงข้อมูล",
                "การกรองข้อมูล",
                "การค้นหาข้อความบางส่วน",
                "การกรองช่วงตัวเลข",
                "การเรียงลำดับและจำกัดจำนวน",
                "การเชื่อมหลายตาราง",
                "ฟังก์ชันคำนวณค่าสรุป",
                "คำสั่งซ้อนขั้นสูง"
        );

        assertEquals(expectedCategories, knowledgeCenter.getCategories());
        for (String category : expectedCategories) {
            assertFalse(
                    knowledgeCenter.getTopicsByCategory(category).isEmpty(),
                    "Category should have at least one topic: " + category
            );
        }
    }

    @Test
    void testEveryTopicContainsAllRequiredSixContentSections() {
        List<SqlTopic> topics = knowledgeCenter.getAllTopics();
        assertEquals(11, topics.size());

        for (SqlTopic topic : topics) {
            assertFalse(topic.getId().isBlank());
            assertFalse(topic.getCategory().isBlank());
            assertFalse(topic.getTitle().isBlank());
            assertFalse(topic.getRelatedConcepts().isEmpty());
            assertFalse(topic.getConceptOverview().isBlank());
            assertFalse(topic.getSyntax().isBlank());
            assertFalse(topic.getExplanation().isBlank());
            assertFalse(topic.getExampleQuery().isBlank());
            assertFalse(topic.getExpectedResult().isBlank());
            assertFalse(topic.getCommonMistakes().isBlank());
        }
    }

    @Test
    void testLookupByIdCategoryAndNullSafety() {
        assertTrue(knowledgeCenter.getTopicById("kc04").isPresent());
        assertEquals("LIKE & % — ค้นหาคำบางส่วน", knowledgeCenter.getTopicById("KC04").orElseThrow().getTitle());
        assertTrue(knowledgeCenter.getTopicById(null).isEmpty());
        assertTrue(knowledgeCenter.getTopicById("   ").isEmpty());
        assertTrue(knowledgeCenter.getTopicById("KC999").isEmpty());

        assertEquals(2, knowledgeCenter.getTopicsByCategory(" การกรองข้อมูล ").size());
        assertTrue(knowledgeCenter.getTopicsByCategory(null).isEmpty());
        assertTrue(knowledgeCenter.getTopicsByCategory("Unknown Category").isEmpty());
    }

    @Test
    void testMapsMultipleConceptsPerMissionAcrossAllSqlLevels() {
        for (int cp = 3; cp <= 7; cp++) {
            try (DatabaseLevel level = new DatabaseLevel(cp)) {
                level.loadChallenge();
                List<SqlTopic> matched = knowledgeCenter.findTopicsForLevel(level);
                assertTrue(
                        matched.size() >= 2,
                        "Checkpoint " + cp + " teaches multiple concepts and should match multiple topics"
                );
                assertEquals(
                        matched.stream().map(SqlTopic::getId).distinct().count(),
                        matched.size(),
                        "Matched topics should not contain duplicates"
                );
            }
        }

        try (DatabaseLevel m03 = new DatabaseLevel(5)) {
            m03.loadChallenge();
            List<String> m03Ids = knowledgeCenter.findTopicsForLevel(m03)
                    .stream()
                    .map(SqlTopic::getId)
                    .toList();
            assertEquals(List.of("KC01", "KC02", "KC05", "KC06", "KC07"), m03Ids);
        }

        try (DatabaseLevel m05 = new DatabaseLevel(7)) {
            m05.loadChallenge();
            List<String> m05Ids = knowledgeCenter.findTopicsForLevel(m05)
                    .stream()
                    .map(SqlTopic::getId)
                    .toList();
            assertEquals(List.of("KC01", "KC02", "KC03", "KC08", "KC09", "KC10", "KC11"), m05Ids);
        }

        assertTrue(knowledgeCenter.findTopicsForLevel(null).isEmpty());
        assertTrue(knowledgeCenter.findTopicsForConcepts(null).isEmpty());
        assertTrue(knowledgeCenter.findTopicsForConcepts(List.of()).isEmpty());
    }

    @Test
    void testExamplesUseSeparateSampleDataAndNeverRevealMissionTables() {
        Set<String> missionTables = Set.of(
                "explorers", "expedition_logs", "inscriptions", "locations", "artifacts", "experiments"
        );

        for (SqlTopic topic : knowledgeCenter.getAllTopics()) {
            String combined = (topic.getExampleQuery() + "\n" + topic.getExpectedResult())
                    .toLowerCase(Locale.ROOT);
            for (String table : missionTables) {
                Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(table) + "\\b");
                assertFalse(
                        wordPattern.matcher(combined).find(),
                        "Knowledge Center topic " + topic.getId() + " must not use mission table: " + table
                );
            }
        }
    }

    @Test
    void testReturnedCollectionsAreUnmodifiable() {
        List<SqlTopic> all = knowledgeCenter.getAllTopics();
        List<String> categories = knowledgeCenter.getCategories();
        List<SqlTopic> filtered = knowledgeCenter.getTopicsByCategory("การกรองข้อมูล");
        List<SqlTopic> byConcepts = knowledgeCenter.findTopicsForConcepts(List.of("SELECT", "WHERE"));
        List<String> concepts = all.get(0).getRelatedConcepts();

        assertThrows(UnsupportedOperationException.class, () -> all.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> categories.add("Extra"));
        assertThrows(UnsupportedOperationException.class, () -> filtered.clear());
        assertThrows(UnsupportedOperationException.class, () -> byConcepts.clear());
        assertThrows(UnsupportedOperationException.class, () -> concepts.add("EXTRA"));
    }

    @Test
    void testEveryExampleQueryMatchesSampleLibraryResults() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE authors (author_id INTEGER PRIMARY KEY, author_name TEXT, country TEXT);");
            stmt.execute("CREATE TABLE books (book_id INTEGER PRIMARY KEY, title TEXT, author_id INTEGER, genre TEXT, pages INTEGER, publish_year INTEGER);");
            stmt.execute("INSERT INTO authors VALUES (1, 'Maya Lin', 'Taiwan'), (2, 'Kenji Sato', 'Japan'), (3, 'Elena Ruiz', 'Spain');");
            stmt.execute("INSERT INTO books VALUES "
                    + "(101, 'SQL Basics', 1, 'Database', 180, 2021),"
                    + "(102, 'Starlight Tales', 2, 'Fiction', 320, 2020),"
                    + "(103, 'Data Modeling Guide', 1, 'Database', 260, 2023),"
                    + "(104, 'Ocean Atlas', 3, 'Science', 410, 2022),"
                    + "(105, 'Query Patterns', 2, 'Database', 210, 2024);");

            for (SqlTopic topic : knowledgeCenter.getAllTopics()) {
                List<List<String>> actualRows = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery(topic.getExampleQuery())) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();
                    while (rs.next()) {
                        List<String> row = new ArrayList<>();
                        for (int c = 1; c <= cols; c++) {
                            row.add(rs.getString(c));
                        }
                        actualRows.add(row);
                    }
                }

                String[] lines = topic.getExpectedResult().split("\\R");
                assertTrue(lines.length >= 3, "Expected table output for " + topic.getId());
                List<List<String>> expectedRows = new ArrayList<>();
                for (int i = 2; i < lines.length; i++) {
                    String[] parts = lines[i].split("\\|");
                    List<String> row = new ArrayList<>();
                    for (String part : parts) {
                        row.add(part.trim());
                    }
                    expectedRows.add(row);
                }

                assertEquals(expectedRows, actualRows, "Expected result mismatch for topic " + topic.getId());
            }
        }
    }

    @Test
    void testSqlTopicRejectsNullOrBlankIdentifiersAndCategory() {
        assertThrows(NullPointerException.class, () -> new SqlTopic(
                null, "Basic Queries", "Title", List.of("SELECT"),
                "overview", "syntax", "explanation", "SELECT 1", "1\n-\n1", "mistakes"
        ));
        assertThrows(IllegalArgumentException.class, () -> new SqlTopic(
                "   ", "Basic Queries", "Title", List.of("SELECT"),
                "overview", "syntax", "explanation", "SELECT 1", "1\n-\n1", "mistakes"
        ));
        assertThrows(IllegalArgumentException.class, () -> new SqlTopic(
                "KC99", "  ", "Title", List.of("SELECT"),
                "overview", "syntax", "explanation", "SELECT 1", "1\n-\n1", "mistakes"
        ));
        assertThrows(IllegalArgumentException.class, () -> new SqlTopic(
                "KC99", "Basic Queries", "", List.of("SELECT"),
                "overview", "syntax", "explanation", "SELECT 1", "1\n-\n1", "mistakes"
        ));
    }
}
