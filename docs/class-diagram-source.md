# CipherQuest — ต้นฉบับ Mermaid

อ่านเอกสารพร้อมภาพได้ที่ [Class Diagram](class-diagram.md) แก้แผนภาพที่ไฟล์นี้ แล้วสร้าง SVG ใหม่ในโฟลเดอร์ `docs/diagrams` ด้วย Mermaid CLI

```sh
mkdir -p target
mmdc -i docs/class-diagram-source.md -o target/class-diagram.md -a docs/diagrams -e svg -b white -c docs/diagrams/mermaid-config.json
```

## 1. ภาพรวมแพ็กเกจของ CipherQuest

```mermaid
flowchart TD
    Main["Main"] --> UI["ui<br/>ScreenNavigator และ controller"]
    UI --> Core["core<br/>GameSession / GameEngine"]
    Core --> Level["levels<br/>LevelFactory / LevelManager"]
    Core --> Knowledge["knowledge<br/>SqlKnowledgeCenter"]
    Core --> Player["player / clues<br/>Player / ClueArchive"]
    Level --> Resources["util<br/>ResourceLoader"]
    Knowledge --> Resources
    Player --> Resources
```

## 2. คลาสหน้าด่านและระบบช่วยผู้เล่น

```mermaid
classDiagram
    class BaseMissionController {
        <<abstract>>
        #initializeMissionHeader()
        #onOpenHints()
        #proceedAfterFeedback(Node node, AnswerResult result, Duration delay)
    }

    class PasskeyScreenController
    class TerminalScreenController
    class SqlWorkbenchController
    class ScreenNavigator {
        +navigateToCurrentLevel()$
        +navigateToVictoryScreen()$
    }

    class MissionSupportDialog {
        ~showHints(Node owner, GameSession session)$
        ~showKnowledge(Node owner, List~SqlTopic~ topics)$
        ~showClues(Node owner, List~Clue~ clues)$
    }

    class GameSession {
        +hintProgress() HintProgress
        +revealHint() HintProgress
        +currentSqlTopics() List~SqlTopic~
        +discoveredClues() List~Clue~
        +currentMissionClue() Optional~Clue~
    }

    class HintProgress {
        <<record>>
        List~String~ revealed
        int total
        boolean canReveal
    }

    class SqlKnowledgeCenter {
        +findTopicsForLevel(Level level) List~SqlTopic~
    }

    class SqlTopic
    class GameEngine

    BaseMissionController <|-- PasskeyScreenController
    BaseMissionController <|-- TerminalScreenController
    BaseMissionController <|-- SqlWorkbenchController
    BaseMissionController ..> MissionSupportDialog : opens hints
    SqlWorkbenchController ..> MissionSupportDialog : opens topics and clues
    BaseMissionController ..> ScreenNavigator : reads and advances mission
    MissionSupportDialog ..> GameSession : reveals hints
    ScreenNavigator --> GameSession : SESSION
    GameSession --> GameEngine
    GameSession ..> HintProgress : returns
    GameSession *-- SqlKnowledgeCenter
    SqlKnowledgeCenter --> SqlTopic : catalog
```

## 3. ลำดับชั้นของด่านและการโหลดข้อมูล

```mermaid
classDiagram
    class LevelFactory {
        <<interface>>
        +createLevel(int checkpointId) Level
        +getTotalCheckpoints() int
    }

    class LevelManager
    class Level {
        <<interface>>
        +loadChallenge()
        +checkAnswer(String input) boolean
        +getHints() List~String~
        +useHint() boolean
        +getScreenType() ScreenType
    }

    class BaseLevel {
        <<abstract>>
        -int checkpointId
        -int attempts
        -int currentHintLevel
        -boolean hintUsed
        -boolean completed
        #loadCommonFields(ChallengeDefinition challenge)
        #recordAttempt()
    }

    class CaesarLevel {
        -String ciphertext
        +getCiphertext() String
    }

    class LinuxLevel {
        -VirtualTerminal terminal
        +getTerminal() VirtualTerminal
    }

    class DatabaseLevel {
        -SqlDatabaseEnvironment dbEnv
        -QueryResult lastQueryResult
        +getTask() String
        +getLastQueryResult() QueryResult
    }

    class ChallengeDefinition {
        +load(int checkpoint) ChallengeDefinition$
        +required(String key) String
        +list(String key) List~String~
    }

    class SqlDatabaseEnvironment {
        +executeQuery(String sql) QueryResult
    }

    class ResourceLoader {
        +loadProperties(String path) Properties$
        +loadString(String path) String$
    }

    LevelFactory <|.. LevelManager
    LevelManager ..> Level : creates
    Level <|.. BaseLevel
    BaseLevel <|-- CaesarLevel
    BaseLevel <|-- LinuxLevel
    BaseLevel <|-- DatabaseLevel
    BaseLevel ..> ChallengeDefinition : reads common fields
    DatabaseLevel *-- SqlDatabaseEnvironment
    ChallengeDefinition ..> ResourceLoader : reads checkpoint properties
    SqlDatabaseEnvironment ..> ResourceLoader : reads seed SQL
```

## 4. เส้นทางคำสั่งของ Linux Terminal

```mermaid
flowchart LR
    Level["LinuxLevel"] --> Terminal["VirtualTerminal"]
    Terminal <-->|"stdin / stdout"| Worker["terminal/worker.mjs"]
    Worker -->|"runs command"| Bash["just-bash"]
    Worker -->|"seeds mission files"| Mission["terminal/mission.mjs"]
    Bash --> Filesystem["InMemoryFs"]
    Mission --> Filesystem
```

## 5. คลาส Game Engine และความคืบหน้า

```mermaid
classDiagram
    class GameEngine {
        <<Singleton>>
        -GameState gameState
        -int currentCheckpoint
        -Level currentLevel
        +getInstance() GameEngine$
        +startNewGame()
        +submitAnswer(String input) AnswerResult
        +advanceToNextCheckpoint() boolean
        +requestHint() String
        +saveProgress() boolean
        +loadSavedGame() boolean
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
        +onGameStateChanged(GameState oldState, GameState newState)
        +onLevelLoaded(Level level)
        +onAnswerEvaluated(AnswerResult result)
        +onScoreChanged(int currentScore, int delta)
        +onHintUsed(String hint)
        +onClueDiscovered(Clue clue)
        +onGameCompleted(Player player)
    }

    class ScoreCalculator {
        <<interface>>
        +calculateScore(int checkpointId, int attempts, boolean hintUsed) int
    }

    class DefaultScoreCalculator
    class StrictScoreCalculator
    class ProgressSaveManager
    class LevelFactory {
        <<interface>>
    }

    class Player
    class ClueArchive {
        +unlockMainClueForCheckpoint(int checkpointId) Optional~Clue~
        +findDiscoveredMainClue(int checkpointId) Optional~Clue~
        +getAllDiscoveredClues() List~Clue~
    }

    class Clue
    class AnswerResult {
        -boolean correct
        -Status status
        -String message
        -int pointsAwarded
        -int currentScore
    }

    GameEngine --> GameState : tracks state
    GameEngine --> LevelFactory : creates levels through
    GameEngine o-- GameEventListener : notifies
    GameEngine --> ScoreCalculator : computes score
    ScoreCalculator <|.. DefaultScoreCalculator
    ScoreCalculator <|.. StrictScoreCalculator
    GameEngine --> ProgressSaveManager : saves and restores progress
    GameEngine --> Player
    Player *-- ClueArchive
    ClueArchive --> Clue : catalog and discoveries
    GameEngine ..> AnswerResult : returns
```
