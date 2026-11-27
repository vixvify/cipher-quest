# CipherQuest

เกมผจญภัยแก้ปริศนาที่ผสมผสานการเรียนรู้ Cryptography, Linux Commands และ SQL

## วิธีการรันเกม

### วิธีที่ 1: ใช้ Maven
```bash
npm ci --prefix terminal --ignore-scripts --omit=optional
mvn javafx:run
```

### วิธีที่ 2: ใช้สคริปต์ (Windows)
รันจากโฟลเดอร์โปรเจกต์ใน Command Prompt

```bat
scripts\run-game.bat
```

หรือรันใน PowerShell

```powershell
.\scripts\run-game.ps1
```

รันคำสั่ง Maven จากโฟลเดอร์หลักของโปรเจกต์ ใช้ `mvn test` หลังติดตั้งแพ็กเกจของ Terminal เพื่อทดสอบรวมทั้งด่าน Linux

### ใช้ Docker

เปิด Docker Desktop หรือ Docker Engine แล้วรันจากโฟลเดอร์โปรเจกต์

```sh
docker compose up --build --wait
```

หลังคำสั่งเสร็จ ให้เปิด [หน้าต่าง CipherQuest](http://localhost:6080/vnc.html?autoconnect=1&resize=scale) แล้วคลิกหน้าจอเกมเพื่อส่งคีย์บอร์ดเข้าเกม ใช้ `docker compose logs -f game` เพื่อดูสถานะ หรือ `docker compose down` เพื่อหยุดเกม

Compose เปิดพอร์ตเฉพาะบนเครื่องที่รัน Docker เปลี่ยนพอร์ตได้ด้วย `CIPHERQUEST_PORT` และเซฟอยู่ใน Docker volume `saves` หน้าต่างผ่าน noVNC ส่งภาพ คีย์บอร์ด และเมาส์ เสียงเกมใช้ได้เมื่อเปิดเกมแบบเดสก์ท็อปบนเครื่องที่มีอุปกรณ์เสียง

ถ้าต้องการใช้จอ X11 ของเครื่องโดยตรง ให้ build เป้าหมาย `desktop` แล้วรันจากเซสชัน Linux ที่มี `DISPLAY` และไฟล์ Xauthority ของผู้ใช้

```sh
docker build --target desktop -t cipherquest .
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
  - `ui/menu/` ดูแลหน้าเริ่มเกมและหน้าสรุป
  - `ui/exploration/` ดูแลหน้าสำรวจ การวาดแผนที่ และหน้าต่างกิจกรรม
  - `ui/mission/` ดูแลหน้าถอดรหัส Terminal และ SQL
  - `ui/shared/` เก็บเสียง เอฟเฟกต์ และหน้าต่างช่วยเหลือที่ใช้ร่วมกัน
- `core/GameSession` รับคำสั่งจาก UI และส่งข้อมูลสำหรับแสดงผล ส่วน `core/GameEngine` ดูแลสถานะเกม คะแนน การปลดล็อกด่าน และการบันทึก
- `levels/` ตรวจคำตอบตามชนิดด่านผ่านสัญญา `Level` และใช้สถานะร่วมจาก `BaseLevel`
- `exploration/` โหลดแผนที่โถงและดูแลการเดิน การชน และระยะโต้ตอบ โดยรับสถานะปลดล็อกจากเกมผ่าน UI
- แก้ตำแหน่งสถานี ประตู และสิ่งกีดขวางใน `resources/data/exploration/archive.map` ส่วนชื่อสถานีอยู่ใน `stations.properties`
- วางหลักฐานของแต่ละสถานีและกำหนดข้อความใน `resources/data/exploration/field-notes.properties`
- `terminal/` เชื่อมด่าน Linux กับ just-bash และเก็บไฟล์เบาะแสเริ่มต้นของ shell
- แก้ชื่อ โจทย์ คำใบ้ หรือคำตอบของด่านได้ใน `resources/data/challenges/checkpoint-*.properties` ซึ่งอ่านด้วย UTF-8

## คุณสมบัติของเกม

- **โถงสำรวจ 2D** เดินด้วย WASD หรือปุ่มลูกศร กด E ที่เอกสารสีทองเพื่อเก็บข้อมูลลงสมุดภาคสนาม แล้วใช้ข้อมูลที่โต๊ะสีฟ้าเพื่อกู้เบาะแส สถานีสีเขียวใช้เข้าภารกิจตามลำดับ กด Esc เพื่อกลับมาสำรวจระหว่างเล่น
- **กิจกรรมบนแผนที่** กด E ที่กล่องจ่ายไฟ ลานสินค้า หรือแผงนิรภัยเพื่อเริ่ม ต่อไฟและใช้จุดตรวจด้วย E เข็นลังด้วยการเดินชนจากด้านที่ต้องการ กด R เพื่อเริ่มกิจกรรมใหม่ หรือ Esc เพื่อออกจากกิจกรรม
- **เสียง** เพลง Lobby เล่นต่อเนื่องระหว่างเปลี่ยนหน้าจอ สลับเปิดเพลงและเสียงเอฟเฟกต์ได้จาก Home และโถงสำรวจ
- 🔐 **Caesar Cipher Level** - เรียนรู้การเข้ารหัสแบบ Caesar
- 💻 **Linux Terminal Level** - ใช้คำสั่ง Bash, pipe และ redirection ในระบบไฟล์เสมือนของ just-bash
- 🗄️ **SQL Database Level** - ฝึกเขียน SQL queries
- 🎮 **Interactive UI** - เมนู pixel art และหน้าภารกิจ JavaFX สำหรับการถอดรหัส Terminal และ SQL

## License

MIT License
