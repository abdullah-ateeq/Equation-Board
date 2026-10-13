# Equation Board 🧮

[![Android SDK](https://img.shields.io/badge/Android-SDK%2037-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575?style=flat&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Equation Board** is a modern, native Android arithmetic puzzle game built with **Jetpack Compose** and **Material 3**. Construct arithmetic equations from a 5×5 grid of number tiles using addition, subtraction, and multiplication to match target numbers!

> *"Build the equation. Reach the target."*

---

## 🌟 Highlights

- **🎯 Guaranteed Solvability**: Advanced target generation and solver algorithms ensure every target presented has at least one valid solution on the active board.
- **🔢 2 & 3 Number Equations**: Support for simple 2-number equations (e.g., `4 + 5 = 9`) as well as challenging 3-number equations (e.g., `6 × 3 − 2 = 16`).
- **💡 Hints & Lifelines**: Includes up to 3 **Skips**, 2 **Reshuffles**, and optional **Smart Hints** per 10-target round.
- **🎨 Modern Material 3 Design**: Fully supports Light Theme, Dark Theme, System Default, and Android 12+ **Dynamic Color** themes.
- **📊 Detailed Statistics**: Tracks games played, completed games, best score, accuracy percentage, perfect runs, and detailed solve breakdowns.
- **🔊 Tactile & Audio Feedback**: Customizable sound effects and subtle haptic feedback for button taps, tile selections, and target completions.
- **🔒 100% Offline & Private**: Zero permissions required. No internet access, no tracking, and no ads.

---

## 🎮 How to Play

1. **Target**: A goal target number is displayed at the top of the game screen.
2. **Select Tiles**: Tap numbers from the 5×5 tile board.
3. **Choose Operations**: Select arithmetic operations (`+`, `−`, `×`).
4. **Check Equation**: Tap **CHECK** to validate your equation.
5. **Score & Refresh**: Correct equations earn points, remove consumed tiles, and refill empty board spots with fresh numbers. Reach 10 targets to complete the game!

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 components
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Architecture**: MVVM (Model-View-ViewModel) powered by Kotlin `StateFlow` and Coroutines
- **Storage**: `SharedPreferences` / `PreferencesManager` for persistent settings & statistics
- **Testing**: JUnit unit tests for equation engine and solution generator logic (`EquationBoardGameLogicTest`)

---

## 📁 Project Structure

```text
EquationBoard/
├── app/
│   └── src/
│       ├── main/
│       │   └── java/com/example/equationboard/
│       │       ├── data/             # Preferences, Data models, Settings & Statistics
│       │       ├── game/             # ViewModel & State Management
│       │       │   ├── logic/        # Equation Engine, Target Generator, Board Generator & Solution Finder
│       │       │   └── model/        # Board models (Cell, Equation, Target, GamePhase)
│       │       ├── navigation/       # Compose Navigation setup & screens mapping
│       │       ├── ui/               # Composable UI components, screens & themes
│       │       │   ├── components/   # NumberTile, TargetCard, EquationPreviewBar, etc.
│       │       │   ├── screens/      # HomeScreen, GameScreen, ResultScreen, SettingsScreen, AboutScreen
│       │       │   └── theme/        # Color schemes, Type, and Material3 Theme definitions
│       │       └── util/             # SoundHelper & HapticHelper utilities
│       └── test/                     # Unit test suites
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer recommended.
- **JDK**: Version 11 or higher.
- **Android Device / Emulator**: Running Android 7.0 (API 24) or higher.

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Equation-Board.git
   cd Equation-Board
   ```

2. **Open in Android Studio**:
   - Open Android Studio, choose **Open**, and navigate to the `EquationBoard` directory.

3. **Build & Run**:
   - Select the `app` configuration.
   - Click **Run** (`Shift + F10`) or debug on your connected Android device / emulator.

---

## 🧪 Running Unit Tests

Run the unit test suite via Gradle:
```bash
./gradlew test
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
