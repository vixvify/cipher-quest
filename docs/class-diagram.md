# CipherQuest — Class Diagram

## 1. ภาพรวมระบบ (Package Overview)

```mermaid
graph TD
    Main["Main\nEntry Point"]

    subgraph UI["ui"]
        SN["ScreenNavigator"]
        MM["MainMenuController"]
        PK["PasskeyScreenController"]
        TM["TerminalScreenController"]
        SQL["SqlWorkbenchController"]
    end

    subgraph Core["core"]
        GE["GameEngine\nSingleton"]
        LM["LevelManager"]
        SC["ScoreCalculator"]
        PS["ProgressSaveManager"]
    end

    subgraph Levels["levels"]
        CL["CaesarLevel\ncheckpoint 1"]
        LL["LinuxLevel\ncheckpoint 2"]
        DL["DatabaseLevel\ncheckpoint 3-7"]
    end

    subgraph PlayerPkg["player + clues"]
        P["Player"]
        CA["ClueArchive"]
    end

    Main --> SN
    SN --> GE
    MM --> GE
    PK --> GE
    TM --> GE
    SQL --> GE

    GE --> LM
    GE --> SC
    GE --> PS
    GE --> P

    LM --> CL
    LM --> LL
    LM --> DL

    P --> CA
```

---

## 2. Level Hierarchy — Template Method Pattern

```mermaid
classDiagram
    class Level {
        <<interface>>
        +loadChallenge()
        +checkAnswer(input) bool
        +getHint() String
        +getScreenType() ScreenType
    }

    class BaseLevel {
        <<abstract>>
        -checkpointId int
        -attempts int
        -hintUsed bool
        -completed bool
        #recordAttempt()
        #resetProgressState()
    }

    class CaesarLevel {
        -ciphertext String
        -expectedPlaintext String
        -shift int
        +getCiphertext() String
    }

    class LinuxLevel {
        -terminal VirtualTerminal
        +getTerminal() VirtualTerminal
    }

    class DatabaseLevel {
        -dbEnv SqlDatabaseEnvironment
        -task String
        -expectedQuery String
        -lastQueryResult QueryResult
        +getTask() String
    }

    Level <|.. BaseLevel
    BaseLevel <|-- CaesarLevel : checkpoint 1
    BaseLevel <|-- LinuxLevel : checkpoint 2
    BaseLevel <|-- DatabaseLevel : checkpoint 3-7
```

---

## 3. Linux Terminal — Command Pattern

> `VirtualTerminal` จำลอง Linux terminal ใน RAM — รับ input → parse → dispatch → คืน output โดยไม่แตะ filesystem จริงของเครื่อง

```mermaid
classDiagram
    class VirtualTerminal {
        -commands Map~String,Command~
        -context TerminalContext
        +execute(rawCommand) String
        +reset()
    }

    class Command {
        <<interface>>
        +execute(args, ctx) String
    }

    class TerminalContext {
        filesystem จำลองด้วย Jimfs ใน RAM
        -fileSystem FileSystem
        -currentPath Path
        -targetExecuted bool
        +resolve(input) Path
        +isExecutable(path) bool
        +markTargetExecuted()
    }

    class CdCommand
    class LsCommand
    class CatCommand
    class FindCommand
    class ExecuteCommand
    class OtherCommands["pwd / echo / whoami\nuname / help / clear"]

    VirtualTerminal *-- TerminalContext
    VirtualTerminal o-- Command

    Command <|.. CdCommand
    Command <|.. LsCommand
    Command <|.. CatCommand
    Command <|.. FindCommand
    Command <|.. ExecuteCommand
    Command <|.. OtherCommands
```

---

## 4. Game Engine — Singleton + Observer + State Machine

```mermaid
classDiagram
    class GameEngine {
        <<Singleton>>
        -instance$ GameEngine
        -gameState GameState
        -currentCheckpoint int
        -currentLevel Level
        +getInstance()$ GameEngine
        +startNewGame()
        +submitAnswer(input) AnswerResult
        +advanceToNextCheckpoint()
        +requestHint() String
    }

    class GameState {
        <<enumeration>>
        NOT_STARTED
        PLAYING
        CHECKPOINT_CLEARED
        VICTORY
    }

    class GameEventListener {
        <<interface>>
        +onGameStateChanged()
        +onLevelLoaded()
        +onAnswerEvaluated()
        +onScoreChanged()
        +onClueDiscovered()
    }

    class ScoreCalculator {
        <<interface>>
        +calculateScore() int
    }

    class DefaultScoreCalculator {
        +calculateScore() int
    }

    class AnswerResult {
        -correct bool
        -status Status
        -message String
        -pointsAwarded int
        +correct()$ AnswerResult
        +wrong()$ AnswerResult
        +gameCleared()$ AnswerResult
    }

    GameEngine --> GameState
    GameEngine o-- GameEventListener : notifies
    GameEngine --> ScoreCalculator
    ScoreCalculator <|.. DefaultScoreCalculator
    GameEngine ..> AnswerResult : returns
```

---

## 5. Design Patterns สรุป

| Pattern | Classes | สั้นๆ |
|---|---|---|
| **Singleton** | `GameEngine` | มี instance เดียวตลอด |
| **Template Method** | `BaseLevel` → 3 subclasses | base กำหนด skeleton, subclass override เฉพาะส่วนต่าง |
| **Command** | `Command` ← 11 classes | terminal dispatch ผ่าน `Map` ไม่มี if-else |
| **Strategy** | `ScoreCalculator` | สลับ algorithm คะแนนได้ไม่แตะ Engine |
| **Observer** | `GameEventListener` | Engine ยิง event ไปทุก listener เมื่อ state เปลี่ยน |
| **Factory Method** | `LevelManager.createLevel()` | สร้าง Level subclass ถูกตัวตาม checkpoint id |
| **Facade** | `ScreenNavigator` | ซ่อน FXML/CSS boilerplate ทั้งหมด |
| **Value Object** | `AnswerResult`, `QueryResult`, `Clue` | Immutable + named factory methods |
