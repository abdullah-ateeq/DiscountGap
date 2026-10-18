# DiscountGap 🏷️📊

**DiscountGap** is a modern, high-precision discount and price savings calculator Android app built with **100% Kotlin**, **Jetpack Compose**, and **Material 3**. It enables users to instantly analyze price differences, compute exact monetary savings and percentage discounts, maintain comparison history, and share results effortlessly.

---

## ✨ Features

- ⚡ **Instant Calculation**: Input original and discounted prices to instantly calculate monetary savings, discount percentage, and remaining price ratio.
- 🎯 **High Precision & Overflow-Safe**: Built on exact integer basis-points math (`BigInteger` arithmetic fallback) to completely avoid floating-point rounding inaccuracies.
- 💱 **Multi-Currency & Custom Currency Support**:
  - Pre-configured support for global currencies (USD `$`, EUR `€`, GBP `£`, JPY `¥`, INR `₹`, AED `د.إ`, CAD `$`, AUD `$`, etc.).
  - Custom currency creator with configurable decimal precision (0 to 4 decimal places).
- 🚀 **Quick Presets**:
  - One-tap original price presets ($10, $50, $100, $500, $1000).
  - One-tap discount percentage presets (5%, 10%, 15%, 20%, 25%, 30%, 50%, 70%).
- 📜 **Comparison History**:
  - Save past calculations for quick reference.
  - Search and filter history items.
  - Restore any previous comparison back into the calculator with a single tap.
  - Delete individual items or clear all history with safety confirmation dialogs.
- 📋 **Copy & Share**: Easily copy or share formatted calculation summaries.
- 🎨 **Modern Material 3 Design & Personalization**:
  - Dynamic Color support (Material You).
  - Light, Dark, and System Theme modes.
  - Configurable haptic feedback and transition animations.
  - Persistent user preferences (remembers last state, default currency, etc.).

---

## 🏗️ Architecture & Tech Stack

DiscountGap is built following Modern Android Development (MAD) practices and Clean Architecture principles:

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose with Material 3 components
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture (UI, Domain, Data/Repository)
- **State Management**: Kotlin `StateFlow` and `collectAsStateWithLifecycle`
- **Navigation**: Jetpack Navigation Compose
- **Concurrency**: Kotlin Coroutines
- **Testing**: JUnit 4 with comprehensive unit test coverage for domain logic, financial math, formatters, and ViewModels.
- **Build System**: Gradle KTS with Version Catalogs

---

## 📁 Project Structure

```
DiscountGap/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/example/discountgap/
│   │   │       ├── data/           # Preferences & Persistence layer
│   │   │       ├── domain/         # Business logic, calculator & formatters
│   │   │       ├── model/          # Domain & UI state data models
│   │   │       ├── repository/     # Data repositories (History & Settings)
│   │   │       ├── ui/             # Jetpack Compose UI Screens
│   │   │       │   ├── about/      # About screen
│   │   │       │   ├── history/    # History screen
│   │   │       │   ├── home/       # Home calculator screen
│   │   │       │   ├── settings/   # Settings screen
│   │   │       │   └── theme/      # Material 3 Theme & Typography
│   │   │       └── util/           # Clipboard & Haptic helpers
│   │   └── test/                   # Unit tests suite
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **JDK**: Java 11 or higher
- **Min SDK**: API 26 (Android 8.0)
- **Target SDK**: API 37 (Android 15)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/DiscountGap.git
   cd DiscountGap
   ```

2. **Open in Android Studio**:
   Open Android Studio and select **Open**, then navigate to the cloned `DiscountGap` folder.

3. **Build the project**:
   Build the app using Gradle wrapper:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 🧪 Testing

The repository contains an extensive unit test suite (`DiscountGapUnitTest.kt`) verifying core domain behavior, edge cases (zero price, equal prices, 100% discount, large numbers), currency conversions, and ViewModel state transitions.

Run unit tests via command line:
```bash
./gradlew testDebugUnitTest
```

---

## 👨‍💻 Author

Developed by **Abdullah Ateeq**

GitHub: [@abdullah-ateeq](https://github.com/abdullah-ateeq)
