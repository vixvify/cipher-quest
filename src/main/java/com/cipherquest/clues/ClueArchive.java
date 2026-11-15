package com.cipherquest.clues;

import java.util.*;

public class ClueArchive {

    private final Map<String, Clue> catalog;
    private final Map<Integer, String> checkpointToMainClueId;
    private final Set<String> discoveredClueIds;

    public ClueArchive() {
        this.catalog = new LinkedHashMap<>();
        this.checkpointToMainClueId = new LinkedHashMap<>();
        this.discoveredClueIds = new LinkedHashSet<>();
        initializeCatalog();
    }

    private void initializeCatalog() {
        registerClue(new Clue(
                "MC01",
                "เบาะแสหลัก 01 — นักสำรวจที่หายตัวไป",
                ClueType.MAIN,
                "explorers (E001)",
                "Arin (E001) หายตัวไปหลังจากตรวจพบสัญญาณครั้งสุดท้ายใกล้ Ancient Temple (L001) บันทึกการสำรวจของเขาอาจเปิดเผยสิ่งที่เกิดขึ้นหลังจากนั้น",
                List.of(
                        "นักสำรวจ: Arin",
                        "รหัสนักสำรวจ: E001",
                        "สถานะ: Missing",
                        "สัญญาณล่าสุด: Ancient Temple (L001)",
                        "เบาะแสต่อเนื่อง: ตรวจสอบบันทึกการสำรวจของ Arin"
                )
        ));
        checkpointToMainClueId.put(3, "MC01");

        registerClue(new Clue(
                "MC02",
                "เบาะแสหลัก 02 — ข้อความสุดท้าย",
                ClueType.MAIN,
                "expedition_logs (101)",
                "ข้อความของ Arin ระบุว่าสัญลักษณ์ Sun และ Moon เป็นกุญแจสำคัญในการเปิด Relic Chamber (L002)",
                List.of(
                        "สัญลักษณ์ที่ต้องใช้: Sun and Moon",
                        "จุดหมายถัดไป: Relic Chamber",
                        "รหัสสถานที่: L002"
                )
        ));
        checkpointToMainClueId.put(4, "MC02");

        registerClue(new Clue(
                "MC03",
                "เบาะแสหลัก 03 — โบราณวัตถุที่ถูกลืม",
                ClueType.MAIN,
                "artifacts (A003)",
                "Prime Core (A003) มีระดับพลังงานสูงถึง 100 แต่รายละเอียดการทดลองที่เกี่ยวข้องถูกแยกเก็บไว้ในบันทึกอีกชุดหนึ่ง",
                List.of(
                        "โบราณวัตถุ: Prime Core",
                        "รหัสโบราณวัตถุ: A003",
                        "ระดับพลัง: 100",
                        "ข้อมูลสำคัญ: รายละเอียดการทดลองถูกแยกเก็บไว้ในบันทึกอีกชุดหนึ่ง"
                )
        ));
        checkpointToMainClueId.put(5, "MC03");

        registerClue(new Clue(
                "MC04",
                "เบาะแสหลัก 04 — บันทึกที่ถูกซ่อน",
                ClueType.MAIN,
                "experiments (303)",
                "Prime Core ถูกนำไปใช้ในการทดลอง Energy Amplification แต่ผลลัพธ์ยังไม่สามารถระบุได้ เบาะแสจากการทดลองชี้ไปยัง Archive Vault (L005) ซึ่งเก็บบันทึกชุดสุดท้ายของอารยธรรมโบราณ",
                List.of(
                        "โบราณวัตถุ: Prime Core",
                        "การทดลอง: Energy Amplification",
                        "ผลลัพธ์: Unknown (ไม่สามารถระบุได้)",
                        "จุดหมายถัดไป: Archive Vault",
                        "รหัสสถานที่: L005",
                        "ข้อมูลสำคัญ: เก็บบันทึกชุดสุดท้ายของอารยธรรมโบราณ"
                )
        ));
        checkpointToMainClueId.put(6, "MC04");

        registerClue(new Clue(
                "MC05",
                "เบาะแสหลัก 05 — สมบัติที่สาบสูญ",
                ClueType.MAIN,
                "artifacts (A008) / locations (L005)",
                "Memory Archive คือคลังข้อมูลที่เก็บรักษาความทรงจำ ประวัติศาสตร์ และองค์ความรู้ของอารยธรรมโบราณเอาไว้ นี่คือความหมายที่แท้จริงของ Lost Treasure",
                List.of(
                        "สมบัติ: Memory Archive",
                        "สถานที่: Archive Vault (L005)",
                        "ระดับพลัง: 100",
                        "คำอธิบาย: เก็บรักษาความทรงจำ ประวัติศาสตร์ และองค์ความรู้ของอารยธรรมโบราณ"
                )
        ));
        checkpointToMainClueId.put(7, "MC05");
    }

    private void registerClue(Clue clue) {
        catalog.put(clue.getId(), clue);
    }

    public Optional<Clue> unlockMainClueForCheckpoint(int checkpointId) {
        String clueId = checkpointToMainClueId.get(checkpointId);
        if (clueId == null) {
            return Optional.empty();
        }
        if (discoveredClueIds.add(clueId)) {
            return Optional.ofNullable(catalog.get(clueId));
        }
        return Optional.empty();
    }

    public List<Clue> getDiscoveredMainClues() {
        return getDiscoveredCluesByType(ClueType.MAIN);
    }

    public List<Clue> getAllDiscoveredClues() {
        return getDiscoveredCluesByType(null);
    }

    private List<Clue> getDiscoveredCluesByType(ClueType type) {
        List<Clue> result = new ArrayList<>();
        for (Clue clue : catalog.values()) {
            if (discoveredClueIds.contains(clue.getId()) && (type == null || clue.getType() == type)) {
                result.add(clue);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public boolean hasDiscoveredClue(String clueId) {
        return clueId != null && discoveredClueIds.contains(clueId);
    }

    public Set<String> getDiscoveredClueIds() {
        return Collections.unmodifiableSet(discoveredClueIds);
    }

    public void restoreClueIds(Collection<String> clueIds) {
        discoveredClueIds.clear();
        if (clueIds == null) {
            return;
        }
        for (String clueId : clueIds) {
            if (clueId == null) {
                continue;
            }
            String trimmed = clueId.trim();
            if (catalog.containsKey(trimmed)) {
                discoveredClueIds.add(trimmed);
            }
        }
    }

    public void reset() {
        discoveredClueIds.clear();
    }
}
