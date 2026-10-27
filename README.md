# Memory Hacker

Hacker-themed Simon-style memory game. Java Swing + JDBC (SQLite). Written for **Java 8** (also runs on newer JDKs).

## Run in VS Code
1. Install **JDK 8 or newer** and the **Extension Pack for Java** in VS Code.
2. `File > Open Folder...` and choose the `MemoryHacker` folder.
3. Open `src/Main.java` and click **Run** (or press F5, pick "Memory Hacker").

The SQLite driver is already in `lib/sqlite-jdbc.jar`, and `.vscode/settings.json` registers it. Nothing to install for the database. Scores go to `memoryhacker.db`, created automatically in the folder you run from.

## Run from a terminal / double-click
- Windows: double-click `run.bat` (the window stays open and shows any error). If you only have a JRE (no JDK), it automatically runs the prebuilt `MemoryHacker.jar`.
- Mac/Linux: `./run.sh`
- Any system: `java -jar MemoryHacker.jar` (keep the `lib` folder next to the jar).

Windows tips: if `run.bat` says Java was not found, install a JDK and check `java -version` and `javac -version` in Command Prompt. If you unzip into a protected folder (like `C:\Program Files`), move the folder to your Desktop first.

## How to play
Watch the nodes light up, then repeat the pattern (click, or keys 1-4 for the grid positions).
- Firewall 1: NORMAL, 2: REVERSE (backwards), 3: HIDDEN (buttons shuffle, go by color)
- Firewall 1 = 3 rounds, firewall 2 = 2 rounds, firewall 3 = 1 final round (6 rounds total). Clear all 3 to win, then choose Endless Mode. Change `FIREWALL_ROUNDS` in `game/GameEngine.java` to adjust.
- Hack timer per step (Settings menu: difficulty, timer on/off).
- Power-ups: REPLAY and SLOW-MO, one each per game.
- Banner on every firewall change, BEST score on the HUD, and sound effects (toggle in Settings).

## Project structure (maps to the syllabus)
| Package | Contents | Concepts |
|---|---|---|
| `exceptions` | `WrongPatternException` | custom checked exception, overloaded constructors |
| `game` | `GameEngine`, abstract `Layer` + `NormalLayer`/`ReverseLayer`/`HiddenLayer`, `PowerUp` interface + `Replay`/`SlowMo`, `Ranks`, `ScoreRecord`, generic `Leaderboard<T>` | inheritance, abstract classes, polymorphism, interfaces, generics, collections (`ArrayList`, `TreeMap`), `synchronized` |
| `db` | `Database` (JDBC), `LogWriter` (java.io) | JDBC, `PreparedStatement`, try-with-resources, file I/O |
| `ui` | `GameScreen` (inner class `NodeButton`), `HackTimer`, `SequencePlayer`, `MatrixRain` (threads), `EndScreen`, `LeaderboardScreen`, `HackerFrame` | Swing, event handling, adapters, layout managers (Border, Grid, Flow, Card, GridBag), menus, JTable, multithreading |

Database table: `scores(id, player_name, score, layer_reached, longest_sequence, result, played_at)`.
Ranks: 0-50 SCRIPT KIDDIE, 51-150 PACKET SNIFFER, 151-300 BLACK HAT, 301+ ROOT ACCESS LEGEND.
