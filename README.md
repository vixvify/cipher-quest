# CipherQuest

เกมผจญภัยแก้ปริศนาที่ผสมผสานการเรียนรู้ Cryptography, Linux Commands และ SQL

## วิธีการรันเกม

### วิธีที่ 1: ใช้ Maven
```bash
npm ci --prefix terminal --ignore-scripts --omit=optional
mvn javafx:run
```

### วิธีที่ 2: ใช้สคริปต์ (Windows)
```bash
# Command Prompt
run-game.bat

# PowerShell
.\run-game.ps1
```

รันคำสั่ง Maven จากโฟลเดอร์หลักของโปรเจกต์ ใช้ `mvn test` หลังติดตั้งแพ็กเกจของ Terminal เพื่อทดสอบรวมทั้งด่าน Linux

### ใช้ Docker

```sh
docker build -t cipherquest .
```

เกมเปิดหน้าต่าง JavaFX ผ่าน X11 บน Linux ให้รันจากเซสชันที่มี `DISPLAY` และไฟล์ Xauthority ของผู้ใช้

```sh
mkdir -p "$HOME/.cipherquest"
docker run --rm --user "$(id -u):$(id -g)" --hostname "$(hostname)" \
  -e DISPLAY -e XAUTHORITY=/tmp/cipherquest.Xauthority \
  -e JAVA_TOOL_OPTIONS=-Duser.home=/app/home \
  -v /tmp/.X11-unix:/tmp/.X11-unix:ro \
  -v "${XAUTHORITY:-$HOME/.Xauthority}:/tmp/cipherquest.Xauthority:ro" \
  -v "$HOME/.cipherquest:/app/home/.cipherquest" \
  cipherquest
```

บน Docker Desktop สำหรับ Windows ต้องมี X server ที่อนุญาตให้คอนเทนเนอร์เชื่อมต่อผ่าน TCP จากนั้นตั้ง `DISPLAY` ให้ตรงกับหมายเลขจอ ตัวอย่างนี้ใช้จอ 0 และเก็บเซฟใน Docker volume

```powershell
docker run --rm -e DISPLAY=host.docker.internal:0 -v cipherquest-saves:/home/cipherquest/.cipherquest cipherquest
```

## ข้อกำหนดของระบบ

- Java 25 หรือสูงกว่า
- Maven 3.6 หรือสูงกว่า
- JavaFX 25.0.1
- Node.js 24 LTS และ npm สำหรับด่าน Terminal

## การแก้ปัญหา

### ถ้าเกมไม่เปิด
1. ตรวจสอบว่าติดตั้ง Java 25 แล้ว: `java -version`
2. ตรวจสอบว่าติดตั้ง Maven แล้ว: `mvn -version`
3. Clean และ compile ใหม่: `mvn clean compile`

### ถ้ามี warning เกี่ยวกับ native access
ไม่ต้องกังวล - เกมจะทำงานได้ปกติ warning นี้เป็นเพียงการแจ้งเตือนจาก Java security

### ถ้า Terminal ขึ้นว่าไม่พร้อม

ตรวจ `node --version` แล้วติดตั้งแพ็กเกจด้วย `npm ci --prefix terminal --ignore-scripts --omit=optional` สามารถตั้งตัวแปร `CIPHERQUEST_NODE` เป็น path ของ Node.js ได้หากมีหลายเวอร์ชันบนเครื่อง

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
├── terminal/
└── README.md
```

- `ui/` และ `resources/fxml/`, `resources/css/` ดูแลหน้าจอ การรับอินพุต และการเปลี่ยนหน้า
- `core/GameSession` รับคำสั่งจาก UI และส่งข้อมูลสำหรับแสดงผล ส่วน `core/GameEngine` ดูแลสถานะเกม คะแนน การปลดล็อกด่าน และการบันทึก
- `levels/` ตรวจคำตอบตามชนิดด่านผ่านสัญญา `Level` และใช้สถานะร่วมจาก `BaseLevel`
- `terminal/` เชื่อมด่าน Linux กับ just-bash และเก็บไฟล์เบาะแสเริ่มต้นของ shell
- แก้ชื่อ โจทย์ คำใบ้ หรือคำตอบของด่านได้ใน `resources/data/challenges/checkpoint-*.properties` ซึ่งอ่านด้วย UTF-8

## คุณสมบัติของเกม

- 🔐 **Caesar Cipher Level** - เรียนรู้การเข้ารหัสแบบ Caesar
- 💻 **Linux Terminal Level** - ใช้คำสั่ง Bash, pipe และ redirection ในระบบไฟล์เสมือนของ just-bash
- 🗄️ **SQL Database Level** - ฝึกเขียน SQL queries
- 🎮 **Interactive UI** - เมนู pixel art และหน้าภารกิจ JavaFX สำหรับการถอดรหัส Terminal และ SQL

## License

MIT License
