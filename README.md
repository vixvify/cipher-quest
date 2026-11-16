# CipherQuest

เกมผจญภัยแก้ปริศนาที่ผสมผสานการเรียนรู้ Cryptography, Linux Commands และ SQL

## วิธีการรันเกม

### วิธีที่ 1: ใช้ Maven
```bash
mvn javafx:run
```

### วิธีที่ 2: ใช้สคริปต์ (Windows)
```bash
# Command Prompt
run-game.bat

# PowerShell
.\run-game.ps1
```

### วิธีที่ 3: รันด้วย Java โดยตรง
```bash
# Build ก่อน
mvn clean package

# จากนั้นรัน
mvn javafx:run
```

## ข้อกำหนดของระบบ

- Java 25 หรือสูงกว่า
- Maven 3.6 หรือสูงกว่า
- JavaFX 25.0.1

## การแก้ปัญหา

### ถ้าเกมไม่เปิด
1. ตรวจสอบว่าติดตั้ง Java 25 แล้ว: `java -version`
2. ตรวจสอบว่าติดตั้ง Maven แล้ว: `mvn -version`
3. Clean และ compile ใหม่: `mvn clean compile`

### ถ้ามี warning เกี่ยวกับ native access
ไม่ต้องกังวล - เกมจะทำงานได้ปกติ warning นี้เป็นเพียงการแจ้งเตือนจาก Java security

## โครงสร้างโปรเจกต์

```
cipherquest/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/cipherquest/
│   │   │       ├── Main.java
│   │   │       ├── clues/
│   │   │       ├── core/
│   │   │       ├── knowledge/
│   │   │       ├── levels/
│   │   │       ├── player/
│   │   │       └── ui/
│   │   └── resources/
│   │       ├── css/
│   │       ├── data/
│   │       ├── fxml/
│   │       └── images/
│   └── test/
├── pom.xml
└── README.md
```

## คุณสมบัติของเกม

- 🔐 **Caesar Cipher Level** - เรียนรู้การเข้ารหัสแบบ Caesar
- 💻 **Linux Terminal Level** - ฝึกใช้คำสั่ง Linux ผ่าน Virtual Terminal
- 🗄️ **SQL Database Level** - ฝึกเขียน SQL queries
- 🎮 **Interactive UI** - ออกแบบด้วย JavaFX พร้อม Matrix Rain Effect

## License

MIT License
