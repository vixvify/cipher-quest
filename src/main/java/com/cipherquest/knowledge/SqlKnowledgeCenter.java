package com.cipherquest.knowledge;

import com.cipherquest.levels.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SqlKnowledgeCenter {

    private final List<SqlTopic> topics;
    private final Map<String, SqlTopic> topicsById;
    private final List<String> categories;

    public SqlKnowledgeCenter() {
        List<SqlTopic> builtIn = createDefaultTopics();
        this.topics = List.copyOf(builtIn);

        Map<String, SqlTopic> byId = new LinkedHashMap<>();
        Set<String> categorySet = new LinkedHashSet<>();
        for (SqlTopic topic : builtIn) {
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

    private static List<SqlTopic> createDefaultTopics() {
        return List.of(
                new SqlTopic(
                        "KC01",
                        "Basic Queries",
                        "SELECT & FROM",
                        List.of("SELECT", "FROM"),
                        "SELECT and FROM are the starting point of every data query. SELECT picks which columns you want to read, and FROM names the table that stores them.",
                        "SELECT column1, column2\nFROM table_name;",
                        "- SELECT lists the exact column names separated by commas (or * to show every column).\n"
                                + "- FROM tells the database which table to read from.\n"
                                + "- A semicolon (;) optionally marks the end of the query.",
                        "SELECT title, genre, pages\nFROM books;",
                        "title               | genre    | pages\n"
                                + "--------------------+----------+------\n"
                                + "SQL Basics          | Database | 180\n"
                                + "Starlight Tales     | Fiction  | 320\n"
                                + "Data Modeling Guide | Database | 260\n"
                                + "Ocean Atlas         | Science  | 410\n"
                                + "Query Patterns      | Database | 210",
                        "- Forgetting commas between column names in SELECT.\n"
                                + "- Misspelling a table or column name.\n"
                                + "- Using SELECT * when only specific columns are needed."
                ),
                new SqlTopic(
                        "KC02",
                        "Filtering",
                        "WHERE",
                        List.of("WHERE"),
                        "WHERE filters a table so the query only returns rows that match a specific rule instead of showing everything.",
                        "SELECT column1, column2\nFROM table_name\nWHERE column_name = 'value';",
                        "- WHERE comes immediately after FROM.\n"
                                + "- Single quotes ('...') wrap text values such as 'Database'.\n"
                                + "- Numbers do not need quotes (for example, pages > 200).",
                        "SELECT title, pages\nFROM books\nWHERE genre = 'Database';",
                        "title               | pages\n"
                                + "--------------------+------\n"
                                + "SQL Basics          | 180\n"
                                + "Data Modeling Guide | 260\n"
                                + "Query Patterns      | 210",
                        "- Putting WHERE before FROM.\n"
                                + "- Forgetting single quotes around text values.\n"
                                + "- Using wrong letter casing inside quotes (text comparisons are case-sensitive in SQLite)."
                ),
                new SqlTopic(
                        "KC03",
                        "Filtering",
                        "AND",
                        List.of("AND"),
                        "AND combines two or more conditions inside WHERE so only rows that satisfy every condition at the same time are returned.",
                        "SELECT column1, column2\nFROM table_name\nWHERE condition1 AND condition2;",
                        "- Write the first condition after WHERE, then add AND followed by the next condition.\n"
                                + "- Both sides of AND must be complete comparisons (for example, genre = 'Database' AND publish_year >= 2023).",
                        "SELECT title, publish_year\nFROM books\nWHERE genre = 'Database' AND publish_year >= 2023;",
                        "title               | publish_year\n"
                                + "--------------------+-------------\n"
                                + "Data Modeling Guide | 2023\n"
                                + "Query Patterns      | 2024",
                        "- Writing a second WHERE keyword instead of AND.\n"
                                + "- Leaving out the column name after AND (for example, publish_year = 2023 AND 2024)."
                ),
                new SqlTopic(
                        "KC04",
                        "Pattern Matching",
                        "LIKE & Wildcards (%)",
                        List.of("LIKE", "Wildcards (%)", "Wildcards", "%"),
                        "LIKE searches inside text when you only know part of a word or phrase instead of the full exact value.",
                        "SELECT column1, column2\nFROM table_name\nWHERE text_column LIKE '%keyword%';",
                        "- LIKE replaces = when matching a text pattern.\n"
                                + "- % is a wildcard meaning zero or more of any characters.\n"
                                + "- '%Data%' matches anywhere in the text, 'Data%' matches the start, and '%Guide' matches the end.",
                        "SELECT title, genre\nFROM books\nWHERE title LIKE '%Data%';",
                        "title               | genre\n"
                                + "--------------------+---------\n"
                                + "Data Modeling Guide | Database",
                        "- Using = '%word%' instead of LIKE '%word%' (which checks for literal percent signs).\n"
                                + "- Forgetting the % wildcard, which makes LIKE behave like an exact match."
                ),
                new SqlTopic(
                        "KC05",
                        "Range Filtering",
                        "BETWEEN",
                        List.of("BETWEEN"),
                        "BETWEEN checks whether a value falls inside a minimum-to-maximum range, including both boundary values.",
                        "SELECT column1, column2\nFROM table_name\nWHERE number_column BETWEEN min_value AND max_value;",
                        "- BETWEEN is shorthand for >= min_value AND <= max_value.\n"
                                + "- Always place the smaller number before AND and the larger number after AND.",
                        "SELECT title, pages\nFROM books\nWHERE pages BETWEEN 200 AND 300;",
                        "title               | pages\n"
                                + "--------------------+------\n"
                                + "Data Modeling Guide | 260\n"
                                + "Query Patterns      | 210",
                        "- Reversing the range order (BETWEEN 300 AND 200 returns no rows).\n"
                                + "- Thinking BETWEEN excludes the start or end numbers (both 200 and 300 are included)."
                ),
                new SqlTopic(
                        "KC06",
                        "Sorting and Limiting",
                        "ORDER BY (ASC / DESC)",
                        List.of("ORDER BY", "ASC", "DESC"),
                        "ORDER BY sorts query rows from smallest to largest or largest to smallest based on one or more columns.",
                        "SELECT column1, column2\nFROM table_name\nORDER BY sort_column DESC;",
                        "- ASC sorts ascending (A to Z, smallest to largest) and is the default if omitted.\n"
                                + "- DESC sorts descending (Z to A, largest to smallest).\n"
                                + "- ORDER BY always comes after WHERE.",
                        "SELECT title, pages\nFROM books\nORDER BY pages DESC;",
                        "title               | pages\n"
                                + "--------------------+------\n"
                                + "Ocean Atlas         | 410\n"
                                + "Starlight Tales     | 320\n"
                                + "Data Modeling Guide | 260\n"
                                + "Query Patterns      | 210\n"
                                + "SQL Basics          | 180",
                        "- Putting ORDER BY before WHERE.\n"
                                + "- Forgetting DESC when you want highest-to-lowest order."
                ),
                new SqlTopic(
                        "KC07",
                        "Sorting and Limiting",
                        "LIMIT",
                        List.of("LIMIT"),
                        "LIMIT caps how many rows a query returns, which is especially useful after sorting to grab the top N items.",
                        "SELECT column1, column2\nFROM table_name\nORDER BY sort_column DESC\nLIMIT row_count;",
                        "- LIMIT goes at the very end of the query, after ORDER BY.\n"
                                + "- Pair LIMIT with ORDER BY so you control which rows appear first before cutting off the rest.",
                        "SELECT title, pages\nFROM books\nORDER BY pages DESC\nLIMIT 2;",
                        "title           | pages\n"
                                + "----------------+------\n"
                                + "Ocean Atlas     | 410\n"
                                + "Starlight Tales | 320",
                        "- Placing LIMIT before ORDER BY (which is invalid SQL syntax).\n"
                                + "- Using LIMIT without ORDER BY when looking for the highest or lowest items."
                ),
                new SqlTopic(
                        "KC08",
                        "Table Relationships",
                        "JOIN & ON",
                        List.of("JOIN", "ON"),
                        "JOIN connects two related tables side by side so you can read matching information stored across both tables in one query.",
                        "SELECT table1.col1, table2.col2\nFROM table1\nJOIN table2 ON table1.key_col = table2.key_col;",
                        "- FROM names the first table and JOIN names the second table.\n"
                                + "- ON defines how rows match between the two tables (typically primary key = foreign key).\n"
                                + "- Prefix shared column names with table_name.column_name to avoid ambiguity.",
                        "SELECT books.title, authors.author_name\nFROM books\nJOIN authors ON books.author_id = authors.author_id;",
                        "title               | author_name\n"
                                + "--------------------+------------\n"
                                + "SQL Basics          | Maya Lin\n"
                                + "Starlight Tales     | Kenji Sato\n"
                                + "Data Modeling Guide | Maya Lin\n"
                                + "Ocean Atlas         | Elena Ruiz\n"
                                + "Query Patterns      | Kenji Sato",
                        "- Omitting the ON clause, which multiplies every row from table1 by every row in table2.\n"
                                + "- Selecting a column that exists in both tables without a table prefix (ambiguous column error)."
                ),
                new SqlTopic(
                        "KC09",
                        "Table Relationships",
                        "Table Aliases",
                        List.of("Table Aliases", "AS", "JOIN"),
                        "Table aliases give tables short temporary nicknames inside a query so JOIN and ON conditions stay short and easy to read.",
                        "SELECT t1.col1, t2.col2\nFROM table1 t1\nJOIN table2 t2 ON t1.key_col = t2.key_col;",
                        "- Place a short alias right after the table name (for example, books b and authors a).\n"
                                + "- Use b.column and a.column anywhere in SELECT, ON, WHERE, or ORDER BY.",
                        "SELECT b.title, a.author_name, a.country\nFROM books b\nJOIN authors a ON b.author_id = a.author_id\nWHERE a.country = 'Japan';",
                        "title           | author_name | country\n"
                                + "----------------+-------------+--------\n"
                                + "Starlight Tales | Kenji Sato  | Japan\n"
                                + "Query Patterns  | Kenji Sato  | Japan",
                        "- Mixing the full table name and its alias for the same table in one query.\n"
                                + "- Choosing unclear single-letter aliases when two tables start with the same letter."
                ),
                new SqlTopic(
                        "KC10",
                        "Aggregate Functions",
                        "MAX()",
                        List.of("MAX"),
                        "MAX() calculates the single largest value inside a column across all rows that pass your WHERE filter.",
                        "SELECT MAX(number_column)\nFROM table_name\nWHERE condition;",
                        "- MAX(column) inspects every matching row and returns one aggregate value.\n"
                                + "- You can optionally give the result column a readable label using AS (for example, MAX(pages) AS longest_book_pages).",
                        "SELECT MAX(pages) AS longest_book_pages\nFROM books;",
                        "longest_book_pages\n"
                                + "------------------\n"
                                + "410",
                        "- Using MAX() inside a WHERE clause directly (WHERE pages = MAX(pages) is invalid; use a subquery instead).\n"
                                + "- Expecting MAX() alone to return all details of the winning row."
                ),
                new SqlTopic(
                        "KC11",
                        "Advanced Queries",
                        "Subquery",
                        List.of("Subquery"),
                        "A subquery is a nested SELECT statement wrapped in parentheses inside another query so the outer query can compare against a computed value.",
                        "SELECT column1, column2\nFROM table_name\nWHERE value_column = (SELECT MAX(value_column) FROM table_name);",
                        "- The database runs the inner query in parentheses first and substitutes its single result.\n"
                                + "- The outer query then finds every row whose column matches that result.\n"
                                + "- Keep any shared filters (such as genre or category) inside the subquery too when finding the maximum within a subset.",
                        "SELECT title, pages\nFROM books\nWHERE pages = (SELECT MAX(pages) FROM books);",
                        "title       | pages\n"
                                + "------------+------\n"
                                + "Ocean Atlas | 410",
                        "- Forgetting parentheses around the inner SELECT.\n"
                                + "- Omitting a filter inside the subquery when you only want the maximum for a specific group.\n"
                                + "- Returning multiple columns from a scalar comparison subquery."
                )
        );
    }
}
