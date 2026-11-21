CREATE TABLE locations (
    location_id TEXT PRIMARY KEY,
    location_name TEXT NOT NULL,
    description TEXT NULL
);

INSERT INTO locations (location_id, location_name, description) VALUES
('L001', 'Ancient Temple', 'ซากโบราณสถานบนพื้นดิน จุดที่ตรวจพบสัญญาณล่าสุดของ Arin'),
('L002', 'Relic Chamber', 'ห้องโถงชั้นในที่ถูกปิดผนึกด้วยสัญลักษณ์ Sun และ Moon'),
('L003', 'Observatory Hall', 'หอดูดาวที่พังทลาย พร้อมภาพสลักหินโบราณ'),
('L004', 'Research Sanctum', 'ห้องทดลองลับที่จำกัดสิทธิ์ ใช้ทดสอบโบราณวัตถุพลังงานสูง'),
('L005', 'Archive Vault', 'ห้องนิรภัยใต้ดินชั้นลึก ที่เก็บบันทึกชุดสุดท้ายของอารยธรรมโบราณ');

CREATE TABLE explorers (
    explorer_id TEXT PRIMARY KEY,
    explorer_name TEXT NOT NULL,
    status TEXT NOT NULL,
    clue TEXT NULL
);

INSERT INTO explorers (explorer_id, explorer_name, status, clue) VALUES
('E001', 'Arin', 'Missing', 'พบสัญญาณล่าสุดใกล้ Ancient Temple'),
('E002', 'Lyra', 'Active', 'กำลังสำรวจพื้นที่รอบนอกของซากโบราณสถาน'),
('E003', 'Kael', 'Retired', 'รวบรวมคำแปลจารึกบนพื้นผิวชุดแรกไว้แล้ว'),
('E004', 'Voss', 'Active', 'ดูแลจุดส่งสัญญาณสื่อสารอยู่ที่แคมป์ฐาน');

CREATE TABLE expedition_logs (
    log_id INTEGER PRIMARY KEY,
    explorer_id TEXT NOT NULL,
    location_id TEXT NOT NULL,
    message TEXT NOT NULL,
    clue TEXT NULL,
    FOREIGN KEY (explorer_id) REFERENCES explorers(explorer_id),
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

INSERT INTO expedition_logs (log_id, explorer_id, location_id, message, clue) VALUES
(100, 'E001', 'L001', 'เดินทางมาถึงทางเข้า Ancient Temple ช่วงพลบค่ำ', 'เสาหินด้านนอกตรงกับแผนที่ในคลังข้อมูล'),
(101, 'E001', 'L001', 'ทางเข้าต้องใช้สัญลักษณ์ (symbol) สองอย่างในการเปิด', 'สัญลักษณ์ Sun และ Moon จะช่วยเปิดทางสู่ Relic Chamber (L002)'),
(102, 'E001', 'L002', 'เข้ามาใน Relic Chamber แล้ว ค่าพลังงานสูงผิดปกติ', 'ตรวจพบโบราณวัตถุพลังงานสูงบนแท่นวางตรงกลาง'),
(103, 'E002', 'L001', 'พบเข็มทิศของ Arin ตกอยู่ใกล้ประตูสัญลักษณ์', 'ลองตรวจสอบบันทึกการสำรวจของ E001'),
(104, 'E003', 'L003', 'บันทึกตำแหน่งดวงดาวภายใน Observatory Hall เรียบร้อย', NULL);

CREATE TABLE inscriptions (
    inscription_id INTEGER PRIMARY KEY,
    symbol_type TEXT NOT NULL,
    inscription_text TEXT NOT NULL,
    location_id TEXT NOT NULL,
    clue TEXT NULL,
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

INSERT INTO inscriptions (inscription_id, symbol_type, inscription_text, location_id, clue) VALUES
(201, 'Sun', 'The radiant disc awakens the chamber', 'L001', 'กุญแจดอกแรกของ Relic Chamber'),
(202, 'Moon', 'The silver crescent balances the flame', 'L001', 'กุญแจดอกที่สองของ Relic Chamber');

CREATE TABLE artifacts (
    artifact_id TEXT PRIMARY KEY,
    artifact_name TEXT NOT NULL,
    artifact_type TEXT NOT NULL,
    power_level INTEGER NOT NULL,
    location_id TEXT NOT NULL,
    clue TEXT NULL,
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

INSERT INTO artifacts (artifact_id, artifact_name, artifact_type, power_level, location_id, clue) VALUES
('A001', 'Solar Core', 'Energy Core', 95, 'L002', 'บรรจุพลังงานแสงอาทิตย์'),
('A002', 'Moon Relic', 'Relic', 75, 'L002', 'เชื่อมโยงกับพลังงานดวงจันทร์'),
('A003', 'Prime Core', 'Energy Core', 100, 'L002', 'รายละเอียดการทดลองถูกแยกเก็บไว้ในบันทึกอีกชุดหนึ่ง'),
('A004', 'Cracked Conduit', 'Fragment', 20, 'L002', 'ระดับพลังงานต่ำเกินกว่าจะเปิดใช้งาน'),
('A006', 'Gilded Chalice', 'Treasure', 40, 'L005', 'ภาชนะพิธีกรรมที่มีพลังงานต่ำ'),
('A007', 'Sovereign Diadem', 'Treasure', 70, 'L005', 'ตราสัญลักษณ์ราชวงศ์ที่เก็บรักษาไว้ในห้องนิรภัย'),
('A008', 'Memory Archive', 'Treasure', 100, 'L005', 'เก็บรักษาความทรงจำของอารยธรรมโบราณเอาไว้'),
('A009', 'Vault Sentinel Core', 'Guardian', 100, 'L005', 'ระบบรักษาความปลอดภัยที่เฝ้า Archive Vault');

CREATE TABLE experiments (
    experiment_id INTEGER PRIMARY KEY,
    experiment_type TEXT NOT NULL,
    artifact_id TEXT NOT NULL,
    result TEXT NULL,
    clue TEXT NULL,
    FOREIGN KEY (artifact_id) REFERENCES artifacts(artifact_id)
);

INSERT INTO experiments (experiment_id, experiment_type, artifact_id, result, clue) VALUES
(301, 'Solar Calibration', 'A001', 'Stable', 'รักษาค่าความร้อนให้อยู่ในระดับคงที่'),
(302, 'Lunar Harmonic Test', 'A002', 'Resonant', 'ตอบสนองต่อคลื่นความถี่ของแสงอาทิตย์'),
(303, 'Energy Amplification', 'A003', 'Unknown', 'เปิดทางสู่ Archive Vault ที่ L005 ซึ่งเก็บบันทึกชุดสุดท้ายของอารยธรรมโบราณ');
