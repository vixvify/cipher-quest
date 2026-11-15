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
                        "พื้นฐานการดึงข้อมูล",
                        "SELECT & FROM — เลือกคอลัมน์และตาราง",
                        List.of("SELECT", "FROM"),
                        "SELECT กับ FROM คือจุดเริ่มต้นของทุกคำสั่ง SQL ใช้บอกระบบว่าต้องการอ่านคอลัมน์ไหน และอ่านมาจากตารางชื่ออะไร",
                        "SELECT column1, column2\nFROM table_name;",
                        "- SELECT ใช้ระบุชื่อคอลัมน์ที่ต้องการดู คั่นแต่ละคอลัมน์ด้วยลูกน้ำ , (หรือใช้ * ถ้าอยากดูทุกคอลัมน์)\n"
                                + "- FROM ใช้บอกชื่อตารางที่เก็บข้อมูลนั้นอยู่\n"
                                + "- เครื่องหมาย ; ท้ายประโยคช่วยปิดจบคำสั่งให้ชัดเจน",
                        "SELECT title, genre, pages\nFROM books;",
                        "title               | genre    | pages\n"
                                + "--------------------+----------+------\n"
                                + "SQL Basics          | Database | 180\n"
                                + "Starlight Tales     | Fiction  | 320\n"
                                + "Data Modeling Guide | Database | 260\n"
                                + "Ocean Atlas         | Science  | 410\n"
                                + "Query Patterns      | Database | 210",
                        "- ลืมใส่ลูกน้ำ , คั่นระหว่างชื่อคอลัมน์หลัง SELECT\n"
                                + "- สะกดชื่อตารางหรือชื่อคอลัมน์ผิดแม้แต่ตัวเดียว\n"
                                + "- ใช้ SELECT * ทั้งที่โจทย์ต้องการดูแค่บางคอลัมน์"
                ),
                new SqlTopic(
                        "KC02",
                        "การกรองข้อมูล",
                        "WHERE — กรองแถวตามเงื่อนไข",
                        List.of("WHERE"),
                        "WHERE ใช้คัดกรองข้อมูลในตาราง ให้แสดงเฉพาะแถวที่ตรงกับเงื่อนไขที่เรากำหนด ไม่ต้องดึงออกมาทั้งหมด",
                        "SELECT column1, column2\nFROM table_name\nWHERE column_name = 'value';",
                        "- เขียน WHERE ต่อท้าย FROM เสมอ\n"
                                + "- ถ้าค่าที่เทียบเป็นข้อความ ให้ครอบด้วยเครื่องหมาย '...' เช่น 'Database'\n"
                                + "- ถ้าเป็นตัวเลข ไม่ต้องใส่เครื่องหมายคำพูด เช่น pages > 200",
                        "SELECT title, pages\nFROM books\nWHERE genre = 'Database';",
                        "title               | pages\n"
                                + "--------------------+------\n"
                                + "SQL Basics          | 180\n"
                                + "Data Modeling Guide | 260\n"
                                + "Query Patterns      | 210",
                        "- เอา WHERE ไปวางไว้ก่อน FROM\n"
                                + "- ลืมใส่เครื่องหมาย '...' รอบข้อความ\n"
                                + "- พิมพ์ตัวพิมพ์เล็ก-ใหญ่ใน '...' ไม่ตรงกับข้อมูลจริงในตาราง (SQLite แยกตัวเล็ก-ใหญ่)"
                ),
                new SqlTopic(
                        "KC03",
                        "การกรองข้อมูล",
                        "AND — รวมหลายเงื่อนไขพร้อมกัน",
                        List.of("AND"),
                        "AND ใช้เชื่อมเงื่อนไขตั้งแต่ 2 ข้อขึ้นไปใน WHERE โดยผลลัพธ์ที่ได้ต้องผ่านครบทุกเงื่อนไขพร้อมกัน",
                        "SELECT column1, column2\nFROM table_name\nWHERE condition1 AND condition2;",
                        "- เขียนเงื่อนไขแรกหลัง WHERE แล้วคั่นด้วย AND ตามด้วยเงื่อนไขถัดไป\n"
                                + "- ทั้งสองฝั่งของ AND ต้องเป็นเงื่อนไขที่สมบูรณ์ เช่น genre = 'Database' AND publish_year >= 2023",
                        "SELECT title, publish_year\nFROM books\nWHERE genre = 'Database' AND publish_year >= 2023;",
                        "title               | publish_year\n"
                                + "--------------------+-------------\n"
                                + "Data Modeling Guide | 2023\n"
                                + "Query Patterns      | 2024",
                        "- เขียนคำว่า WHERE ซ้ำอีกรอบแทนที่จะใช้ AND\n"
                                + "- ลืมใส่ชื่อคอลัมน์หลัง AND เช่น publish_year = 2023 AND 2024"
                ),
                new SqlTopic(
                        "KC04",
                        "การค้นหาข้อความบางส่วน",
                        "LIKE & % — ค้นหาคำบางส่วน",
                        List.of("LIKE", "Wildcards (%)", "Wildcards", "%"),
                        "LIKE ใช้ค้นหาข้อความเมื่อเรารู้แค่คำบางส่วนข้างใน ไม่ได้รู้ข้อความทั้งประโยคแบบเป๊ะๆ",
                        "SELECT column1, column2\nFROM table_name\nWHERE text_column LIKE '%keyword%';",
                        "- ใช้ LIKE แทนเครื่องหมาย = เมื่อต้องการเทียบแบบบางส่วน\n"
                                + "- % แทนตัวอักษรอะไรก็ได้กี่ตัวก็ได้ (หรือไม่มีเลยก็ได้)\n"
                                + "- '%Data%' หาคำว่า Data ที่อยู่ตรงไหนก็ได้, 'Data%' หาคำที่ขึ้นต้นด้วย Data, '%Guide' หาคำที่ลงท้ายด้วย Guide",
                        "SELECT title, genre\nFROM books\nWHERE title LIKE '%Data%';",
                        "title               | genre\n"
                                + "--------------------+---------\n"
                                + "Data Modeling Guide | Database",
                        "- ใช้เครื่องหมาย = '%word%' แทน LIKE '%word%' (แบบนั้นจะไปหาข้อความที่มีตัว % จริงๆ)\n"
                                + "- ลืมใส่ % ทำให้ LIKE กลายเป็นเทียบตรงตัวเหมือน ="
                ),
                new SqlTopic(
                        "KC05",
                        "การกรองช่วงตัวเลข",
                        "BETWEEN — กรองข้อมูลเป็นช่วง",
                        List.of("BETWEEN"),
                        "BETWEEN ใช้เช็กว่าค่าอยู่ในช่วงตั้งแต่ค่าน้อยสุดถึงค่ามากสุดหรือไม่ โดยนับรวมทั้งค่าหัวและค่าท้ายด้วย",
                        "SELECT column1, column2\nFROM table_name\nWHERE number_column BETWEEN min_value AND max_value;",
                        "- BETWEEN มีความหมายเหมือน >= min_value AND <= max_value แต่เขียนสั้นกว่า\n"
                                + "- ต้องวางค่าน้อยไว้หน้า AND และค่ามากไว้หลัง AND เสมอ",
                        "SELECT title, pages\nFROM books\nWHERE pages BETWEEN 200 AND 300;",
                        "title               | pages\n"
                                + "--------------------+------\n"
                                + "Data Modeling Guide | 260\n"
                                + "Query Patterns      | 210",
                        "- ใส่ตัวเลขสลับฝั่ง เช่น BETWEEN 300 AND 200 (ผลลัพธ์จะว่างเปล่า)\n"
                                + "- เข้าใจผิดว่า BETWEEN ไม่นับค่าขอบ (จริงๆ นับรวมทั้ง 200 และ 300)"
                ),
                new SqlTopic(
                        "KC06",
                        "การเรียงลำดับและจำกัดจำนวน",
                        "ORDER BY (ASC / DESC) — เรียงลำดับข้อมูล",
                        List.of("ORDER BY", "ASC", "DESC"),
                        "ORDER BY ใช้จัดเรียงแถวผลลัพธ์จากน้อยไปมาก หรือมากไปน้อย ตามคอลัมน์ที่ต้องการ",
                        "SELECT column1, column2\nFROM table_name\nORDER BY sort_column DESC;",
                        "- ASC เรียงจากน้อยไปมาก (A-Z, เลขน้อยไปมาก) ถ้าไม่เขียนจะถือว่าเป็น ASC อัตโนมัติ\n"
                                + "- DESC เรียงจากมากไปน้อย (Z-A, เลขมากไปน้อย)\n"
                                + "- ตำแหน่งของ ORDER BY ต้องอยู่หลัง WHERE เสมอ",
                        "SELECT title, pages\nFROM books\nORDER BY pages DESC;",
                        "title               | pages\n"
                                + "--------------------+------\n"
                                + "Ocean Atlas         | 410\n"
                                + "Starlight Tales     | 320\n"
                                + "Data Modeling Guide | 260\n"
                                + "Query Patterns      | 210\n"
                                + "SQL Basics          | 180",
                        "- เอา ORDER BY ไปวางก่อน WHERE\n"
                                + "- ลืมใส่ DESC ตอนที่ต้องการเรียงจากค่ามากสุดลงมา"
                ),
                new SqlTopic(
                        "KC07",
                        "การเรียงลำดับและจำกัดจำนวน",
                        "LIMIT — จำกัดจำนวนแถว",
                        List.of("LIMIT"),
                        "LIMIT ใช้กำหนดเพดานว่าจะให้แสดงผลลัพธ์สูงสุดกี่แถว มักใช้คู่กับ ORDER BY เพื่อหยิบ Top N อันดับแรก",
                        "SELECT column1, column2\nFROM table_name\nORDER BY sort_column DESC\nLIMIT row_count;",
                        "- LIMIT จะอยู่ท้ายสุดของคำสั่ง ต่อจาก ORDER BY\n"
                                + "- ควรเรียงลำดับด้วย ORDER BY ก่อนใช้ LIMIT เพื่อให้ได้อันดับที่ต้องการจริงๆ",
                        "SELECT title, pages\nFROM books\nORDER BY pages DESC\nLIMIT 2;",
                        "title           | pages\n"
                                + "----------------+------\n"
                                + "Ocean Atlas     | 410\n"
                                + "Starlight Tales | 320",
                        "- วาง LIMIT ไว้ก่อน ORDER BY (ผิดไวยากรณ์ SQL)\n"
                                + "- ใช้ LIMIT เพื่อหาตัวมากสุด/น้อยสุด โดยไม่ได้ใส่ ORDER BY กำกับ"
                ),
                new SqlTopic(
                        "KC08",
                        "การเชื่อมหลายตาราง",
                        "JOIN & ON — เชื่อมข้อมูลสองตาราง",
                        List.of("JOIN", "ON"),
                        "JOIN ใช้จับคู่ข้อมูลจาก 2 ตารางที่มีคอลัมน์อ้างอิงตรงกัน มาวางต่อเป็นแถวเดียวกันในคำสั่งเดียว",
                        "SELECT table1.col1, table2.col2\nFROM table1\nJOIN table2 ON table1.key_col = table2.key_col;",
                        "- FROM ระบุตารางแรก และ JOIN ระบุตารางที่สองที่จะเอามาเชื่อม\n"
                                + "- ON บอกเงื่อนไขจับคู่ระหว่างสองตาราง (มักเป็นรหัส Primary Key = Foreign Key)\n"
                                + "- ถ้าคอลัมน์ชื่อซ้ำกันในทั้ง 2 ตาราง ให้เขียนชื่อตารางนำหน้า เช่น books.author_id",
                        "SELECT books.title, authors.author_name\nFROM books\nJOIN authors ON books.author_id = authors.author_id;",
                        "title               | author_name\n"
                                + "--------------------+------------\n"
                                + "SQL Basics          | Maya Lin\n"
                                + "Starlight Tales     | Kenji Sato\n"
                                + "Data Modeling Guide | Maya Lin\n"
                                + "Ocean Atlas         | Elena Ruiz\n"
                                + "Query Patterns      | Kenji Sato",
                        "- ลืมเขียน ON ทำให้ทุกแถวของสองตารางคูณไขว้กันมั่ว\n"
                                + "- เลือกคอลัมน์ที่มีชื่อซ้ำกันทั้งสองตารางโดยไม่ระบุชื่อตารางนำหน้า (ระบบจะฟ้องว่ากำกวม)"
                ),
                new SqlTopic(
                        "KC09",
                        "การเชื่อมหลายตาราง",
                        "Table Aliases — ตั้งชื่อย่อให้ตาราง",
                        List.of("Table Aliases", "AS", "JOIN"),
                        "Table Alias คือการตั้งชื่อเล่นสั้นๆ ให้ตารางภายในคำสั่ง เพื่อให้เขียน SELECT, JOIN, ON และ WHERE ได้สั้นและอ่านง่ายขึ้น",
                        "SELECT t1.col1, t2.col2\nFROM table1 t1\nJOIN table2 t2 ON t1.key_col = t2.key_col;",
                        "- เว้นวรรคหลังชื่อตารางแล้วตามด้วยชื่อย่อสั้นๆ เช่น books b และ authors a\n"
                                + "- หลังจากตั้งแล้ว ใช้ b.คอลัมน์ และ a.คอลัมน์ ได้ทั้งใน SELECT, ON, WHERE และ ORDER BY",
                        "SELECT b.title, a.author_name, a.country\nFROM books b\nJOIN authors a ON b.author_id = a.author_id\nWHERE a.country = 'Japan';",
                        "title           | author_name | country\n"
                                + "----------------+-------------+--------\n"
                                + "Starlight Tales | Kenji Sato  | Japan\n"
                                + "Query Patterns  | Kenji Sato  | Japan",
                        "- ตั้งชื่อย่อให้ตารางแล้ว แต่ยังเผลอเรียกชื่อตารางเต็มในประโยคเดียวกัน\n"
                                + "- ตั้งชื่อย่อซ้ำหรือสับสนเมื่อชื่อตารางขึ้นต้นด้วยตัวอักษรเดียวกัน"
                ),
                new SqlTopic(
                        "KC10",
                        "ฟังก์ชันคำนวณค่าสรุป",
                        "MAX() — หาค่าสูงสุด",
                        List.of("MAX"),
                        "MAX() ใช้หาค่าตัวเลขที่สูงที่สุดเพียงค่าเดียวในคอลัมน์ จากทุกแถวที่ผ่านเงื่อนไข WHERE",
                        "SELECT MAX(number_column)\nFROM table_name\nWHERE condition;",
                        "- MAX(คอลัมน์) จะคำนวณแล้วคืนค่าสูงสุดออกมา 1 ค่า\n"
                                + "- สามารถใช้ AS ตั้งชื่อหัวคอลัมน์ผลลัพธ์ให้อ่านง่ายได้ เช่น MAX(pages) AS longest_book_pages",
                        "SELECT MAX(pages) AS longest_book_pages\nFROM books;",
                        "longest_book_pages\n"
                                + "------------------\n"
                                + "410",
                        "- เอา MAX() ไปเทียบตรงๆ ใน WHERE เช่น WHERE pages = MAX(pages) ซึ่งทำไม่ได้ ต้องใช้ Subquery แทน\n"
                                + "- คิดว่าเขียน SELECT MAX(pages) ตัวเดียวแล้วจะได้รายละเอียดคอลัมน์อื่นของแถวนั้นติดมาด้วย"
                ),
                new SqlTopic(
                        "KC11",
                        "คำสั่งซ้อนขั้นสูง",
                        "Subquery — เขียน SELECT ซ้อนในเงื่อนไข",
                        List.of("Subquery"),
                        "Subquery คือการนำคำสั่ง SELECT อีกชุดหนึ่งใส่ในวงเล็บ ( ) ไว้ข้างในคำสั่งหลัก เพื่อหาค่ากลางมาก่อน แล้วเอาค่านั้นไปใช้เปรียบเทียบต่อ",
                        "SELECT column1, column2\nFROM table_name\nWHERE value_column = (SELECT MAX(value_column) FROM table_name);",
                        "- ระบบจะรันคำสั่งในวงเล็บก่อนเพื่อให้ได้ค่าคำตอบออกมา 1 ค่า\n"
                                + "- จากนั้นคำสั่งหลักด้านนอกจะดึงแถวที่ตรงกับค่านั้นออกมาทั้งหมด\n"
                                + "- ถ้าต้องการค่าสูงสุดเฉพาะกลุ่ม ต้องใส่เงื่อนไข WHERE เดียวกันไว้ในวงเล็บด้วย",
                        "SELECT title, pages\nFROM books\nWHERE pages = (SELECT MAX(pages) FROM books);",
                        "title       | pages\n"
                                + "------------+------\n"
                                + "Ocean Atlas | 410",
                        "- ลืมใส่วงเล็บ ( ) ครอบคำสั่ง SELECT ตัวใน\n"
                                + "- ลืมใส่เงื่อนไขกรองกลุ่มใน Subquery ทำให้ได้ค่าสูงสุดของทั้งตารางแทนที่จะเป็นของกลุ่มนั้น\n"
                                + "- ให้ Subquery คืนค่ามามากกว่า 1 คอลัมน์ทั้งที่เทียบด้วยเครื่องหมาย ="
                )
        );
    }
}
