# BudgetingSW

> **CS251-2026-19-Sara** — Group project by 20240354, 20240481, 20240671, 20240376

**Repository:** <https://github.com/Ali-Wael-Hassan/BudgetingSW.git>

---

## Table of Contents

1. [Overview](#overview)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Package Structure](#package-structure)
5. [Module Dependencies](#module-dependencies)
6. [Screens & Controllers](#screens--controllers)
7. [Model Layer](#model-layer)
   - [Types (`model.type`)](#types-modeltype)
   - [Records (`model.records`)](#records-modelrecords)
   - [Data Accessors (`model.dataAccessors`)](#data-accessors-modeldataaccessors)
   - [Authentication (`model.authentication`)](#authentication-modelauthentication)
   - [Account Operations (`model.accountOps`)](#account-operations-modelaccountops)
8. [Application State](#application-state)
9. [Event System](#event-system)
10. [Theming](#theming)
11. [Build & Run](#build--run)
12. [JavaDoc](#javadoc)
13. [CS251 Submission Materials](#cs251-submission-materials)

---

## Overview

BudgetingSW is a JavaFX-based desktop personal finance management application. Users can create accounts, log in, track income/expenses, manage budgets, set saving goals, and generate PDF reports. All data is persisted locally as JSON via Jackson serialization.

### Key Features

- **Authentication** — Sign up / log in with encrypted session persistence
- **Dashboard** — Balance overview, monthly spending, budget progress, saving goals
- **Transactions** — Add, view, and filter income/expense records by type
- **Budgets** — Per-category budgets with progress tracking, threshold alerts, and overspend warnings
- **Saving Goals** — Track progress toward financial targets with auto-contributions from income
- **Reports** — Cash flow pie chart, category donut chart, top expenses, savings progress; PDF export
- **Profile** — Edit name/avatar, switch theme (dark/light), change currency (USD/EGP/EUR), change password, delete account
- **Dynamic Categories** — Users can create custom transaction categories

---

## Tech Stack

| Tool         | Version       |
| ------------ | ------------- |
| Java         | JDK 11        |
| Build        | Apache Maven  |
| UI Framework | JavaFX 13     |
| FXML         | Scene Builder |
| Persistence  | Jackson 2.15.2 (JSON) |
| PDF Export   | Apache PDFBox 2.0.29 |
| Encryption   | AES (custom `EncryptionUtil`) |

### Maven Dependencies (`pom.xml`)

```xml
<dependencies>
    <dependency> <!-- JavaFX Controls -->
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>13</version>
    </dependency>
    <dependency> <!-- JavaFX FXML -->
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>13</version>
    </dependency>
    <dependency> <!-- Jackson JSR310 (Java 8 date/time) -->
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>2.15.2</version>
    </dependency>
    <dependency> <!-- PDF Generation -->
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.29</version>
    </dependency>
</dependencies>
```

---

## Architecture

The app follows an **MVC-like architecture**:

- **View** — FXML layouts under `src/main/resources/com/duck/`
- **Controller** — Java classes in `com.duck` that handle screen logic, FXML event wiring, and UI updates via `PropertyChangeListener`
- **Model** — POJOs under `com.duck.model.*` representing domain objects, persistence, and business logic

Communication between layers uses the **observer pattern** (`java.beans.PropertyChangeListener` / `PropertyChangeSupport`):

- `TransactionManager` fires `TRANSACTION_RECEIVED` → `BudgetManager` updates used amounts, `SavingGoalsManager` updates goal progress, `DashboardController` / `ReportsController` refresh UI
- `Session` fires `TOKEN_CHANGED` → all managers reload/clear data on login/logout
- `BudgetManager` fires `BUDGET_EXCEEDED` / `THRESHOLD_REACHED` → `ApplicationState` shows alert dialogs
- `SavingGoalsManager` fires `GOAL_COMPLETED` → `ApplicationState` shows congratulations alert

---

## Package Structure

```
com.duck/
├── App.java                          # JavaFX entry point, scene/window management
├── ApplicationState.java             # Singleton — owns all managers, coordinates events
│
├── LoginController.java              # Sign-in form
├── SignUpController.java             # Registration form
├── DashboardController.java          # Main dashboard
├── TransactionsController.java       # Transaction list & filters
├── BudgetController.java             # Budget grid by category
├── GoalsController.java              # Saving goals overview
├── GoalCardController.java           # Individual goal card (reused in FlowPane)
├── ReportsController.java            # Reports & charts
├── ProfileController.java            # Settings & profile management
│
├── CurrencyUtil.java                 # Currency formatting & conversion (USD/EGP/EUR)
├── DialogHelper.java                 # Modal dialog builders (transactions, budgets, goals, etc.)
├── AvatarHelper.java                 # Avatar rendering (circular clip, SVG fallback)
│
├── model/
│   ├── authentication/
│   │   ├── Auth.java                 # Interface for auth operations
│   │   ├── AppAuth.java              # LocalStorage-backed auth strategy
│   │   ├── Recognition.java          # Abstract template method for auth workflows
│   │   ├── Login.java                # Login validation logic
│   │   ├── SignUp.java               # Registration validation + persistence
│   │   ├── Session.java              # Singleton, persisted encrypted session token
│   │   └── EncryptionUtil.java       # AES encrypt/decrypt utility
│   │
│   ├── accountOps/
│   │   ├── AccountManager.java       # Account CRUD (edit, delete, password change)
│   │   ├── ReportGenerator.java      # Interface for report generation
│   │   └── PDFReport.java            # PDFBox-based transaction report exporter
│   │
│   ├── records/
│   │   ├── Budget.java               # Budget model (category, limit, period, threshold)
│   │   ├── BudgetManager.java        # Budget CRUD + event-driven usedAmount tracking
│   │   ├── TransactionManager.java   # Transaction CRUD + event firing
│   │   └── SavingGoalsManager.java   # Saving goals CRUD + auto-contribution logic
│   │
│   ├── type/
│   │   ├── Account.java              # User account (email, name, password, balance, config)
│   │   ├── AccountConfig.java        # Preferences (avatar, theme mode, currency)
│   │   ├── AppSettings.java          # Enums: DataKey, Mode, Currency, TransactionType, events
│   │   ├── Transaction.java          # Financial transaction (config, date, amount)
│   │   ├── TransactionConfig.java    # Transaction query/creation parameters
│   │   ├── SavingGoal.java           # Saving goal (name, target, current, deadline)
│   │   ├── Period.java               # Date range (start, end) with contains() helper
│   │   ├── Range.java                # Numeric range (min, max) with ordering enforcement
│   │   └── ReportConfig.java         # Report entry (category, percent)
│   │
│   └── dataAccessors/
│       ├── StorageStrategy.java      # Data persistence interface (fetch, save, insert)
│       └── LocalStorage.java         # Singleton — JSON file-backed storage (local_storage.json)
│
└── module-info.java                  # Java module descriptor
```

---

## Module Dependencies

Defined in `src/main/java/module-info.java`:

```java
module com.duck {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.apache.pdfbox;

    opens com.duck.model.records to com.fasterxml.jackson.databind;
    opens com.duck.model.type to com.fasterxml.jackson.databind;
    opens com.duck.model.dataAccessors to com.fasterxml.jackson.databind;
    opens com.duck to javafx.fxml;
    exports com.duck;
    exports com.duck.model.type;
    exports com.duck.model.records;
    exports com.duck.model.dataAccessors;
    exports com.duck.model.authentication;
    exports com.duck.model.accountOps;
}
```

---

## Screens & Controllers

| Screen       | FXML                | Controller              | Purpose |
| ------------ | ------------------- | ----------------------- | ------- |
| Login        | `login.fxml`        | `LoginController`       | Email/password sign-in with visibility toggle and sign-up link |
| Sign Up      | `sign_up.fxml`      | `SignUpController`      | Registration with name, email, password, confirm-password + validation |
| Dashboard    | `dashboard.fxml`    | `DashboardController`   | Balance card, monthly spending card with progress bar, saving goals section |
| Transactions | `transactions.fxml` | `TransactionsController`| Full transaction list grouped by date with All/Income/Expense filter tabs |
| Budgets      | `budget.fxml`       | `BudgetController`      | Month navigation, summary card, per-category budget cards in GridPane |
| Goals        | `goals.fxml`        | `GoalsController`       | Saving goals overview using reusable GoalCard in a FlowPane |
| Goal Card    | `goal_card.fxml`    | `GoalCardController`    | Single goal card — name, amounts, progress bar, days remaining, colored border |
| Reports      | `reports.fxml`      | `ReportsController`     | 2×2 grid: cash flow pie, category donut, top 5 expenses, savings progress |
| Profile      | `profile.fxml`      | `ProfileController`     | Avatar/name, appearance (dark/light), currency, password change, sign out, delete |

**Controller conventions:**
- All screen controllers implement `Initializable` and `PropertyChangeListener`
- Exceptions: `LoginController`, `SignUpController`, `GoalCardController` (no event listening needed)
- Each applies the user's theme on `initialize()` via `App.setTheme()`
- Each has a sidebar with navigation buttons and a circular avatar

---

## Model Layer

### Types (`model.type`)

#### `Account`
| Field | Type | Description |
| ----- | ---- | ----------- |
| `email` | `String` | Primary key / identifier |
| `userName` | `String` | Display name |
| `password` | `String` | Plain text (matched against stored hash) |
| `balance` | `float` | Current account balance |
| `accountConfig` | `AccountConfig` | Preferences (theme, currency, avatar) |

#### `AccountConfig`
| Field | Type | Default | Description |
| ----- | ---- | ------- | ----------- |
| `avatarPath` | `String` | `null` | File path to custom avatar image |
| `mode` | `AppSettings.Mode` | `DARK` | Theme mode |
| `curreny` | `AppSettings.Currency` | `USD` | Preferred currency |

#### `Transaction`
| Field | Type | Description |
| ----- | ---- | ----------- |
| `config` | `TransactionConfig` | Type, period, categories, account |
| `date` | `LocalDate` | Transaction date |
| `amount` | `float` | Monetary amount |

#### `TransactionConfig`
| Field | Type | Description |
| ----- | ---- | ----------- |
| `type` | `TransactionType` | `EXPENSE` or `INCOME` |
| `period` | `Period` | Date range filter |
| `category` | `List<String>` | Assigned categories |
| `range` | `Range` | Amount range filter |
| `account` | `Account` | Owning account |

#### `SavingGoal`
| Field | Type | Description |
| ----- | ---- | ----------- |
| `name` | `String` | Goal name (unique per account, case-insensitive) |
| `targetAmount` | `float` | Target savings amount |
| `currentAmount` | `float` | Current savings |
| `deadline` | `LocalDate` | Target date |
| `account` | `Account` | Owning account |

Methods: `getRemainingAmount()` = target − current; `isActive()` = deadline not past AND current < target.

#### `Budget`
| Field | Type | Description |
| ----- | ---- | ----------- |
| `category` | `String` | Budget category |
| `amount` | `float` | Spending limit |
| `usedAmount` | `float` | Amount spent so far |
| `period` | `Period` | Budget validity period |
| `threshold` | `float` | Warning threshold (e.g., 80 = 80%) |
| `account` | `Account` | Owning account |

Method: `isActive()` returns true if today falls within the budget's period.

#### `Period`
Wraps `startDate` / `endDate` (`LocalDate`) with `contains(LocalDate)` and `getDaysBetween()`.

#### `Range`
Wraps `minValue` / `maxValue` (`float`) with auto-ordering (swaps if min > max) and `contains(float)`.

#### `ReportConfig`
| Field | Type | Description |
| ----- | ---- | ----------- |
| `category` | `String` | Spending/income category |
| `percent` | `float` | Percentage of total |

#### `AppSettings`

Enumerations used across the app:

| Enum | Values |
| ---- | ------ |
| `DataKey` | `ACCOUNTS`, `EXPENSES`, `INCOME`, `BUDGETS`, `CATEGORIES`, `GOALS` |
| `Mode` | `DARK`, `LIGHT` |
| `Currency` | `EGP`, `USD`, `EUR` |
| `TransactionType` | `EXPENSE`, `INCOME` |
| `Message` | `ERROR`, `SUCCESS`, `NOT_FOUND`, `NULL_ACCOUNT`, … (validation messages) |
| `GoalEvent` | `GOAL_COMPLETED` |
| `AccountEvent` | `TOKEN_CHANGED` |
| `TransactionEvent` | `TRANSACTION_RECEIVED` |
| `BudgetEvent` | `THRESHOLD_REACHED`, `BUDGET_EXCEEDED`, `BUDGET_UPDATED` |

---

### Records (`model.records`)

#### `TransactionManager`

Manages the transaction lifecycle.

- **Validation:** Amount > 0, category non-empty, date not future, account non-null
- **`addTransaction()`** — validates, appends to internal list, saves to `LocalStorage` under `EXPENSES` or `INCOME`, auto-creates new categories, fires `TRANSACTION_RECEIVED`
- **`getTransactions(TransactionConfig)`** — filters by account, date period, and type
- Listens for `TOKEN_CHANGED` to load/clear data based on session state

#### `BudgetManager`

Tracks budgets and listens to transactions for automatic updates.

- **Validation:** Category non-empty, amount > 0, usedAmount >= 0, valid period, threshold > 0, no overlapping active budgets for same category+account
- **`createBudget()`** — validates, persists, fires `BUDGET_UPDATED`
- **`editBudget()`** — replaces an existing budget in the list
- **`getAllBudgets(Account)`** — filters budgets by account
- Listens for `TRANSACTION_RECEIVED` — adds expense amounts to `usedAmount`, subtracts income amounts (floored at 0)
- Fires `BUDGET_EXCEEDED` when `usedAmount > amount`, `THRESHOLD_REACHED` when `usedAmount >= (amount × threshold / 100)`

#### `SavingGoalsManager`

Manages saving goals and auto-contributes from income transactions.

- **Validation:** Name non-empty, target > 0, current >= 0 and <= target, deadline non-null and future, only one active goal at a time
- **`createSavingGoal()`** — validates, persists
- **`calculateMonthlySaving()`** — (`target − current`) / months until deadline
- **`getAllSavings(Account)`** — filters by account
- Listens for `TRANSACTION_RECEIVED` — adds positive (income) amounts to the active goal's `currentAmount`
- Fires `GOAL_COMPLETED` when `currentAmount >= targetAmount`

---

### Data Accessors (`model.dataAccessors`)

#### `StorageStrategy` (Interface)

| Method | Signature | Description |
| ------ | --------- | ----------- |
| `fetch` | `Object fetch(DataKey key)` | Retrieve a stored list by key |
| `save` | `Message save(DataKey key, Object data)` | Replace entire list for a key |
| `insert` | `Message insert(DataKey key, Object data)` | Append an item to a list |

#### `LocalStorage` (Singleton)

JSON file-based persistence implementation.

- **File:** `local_storage.json` in the project root directory
- **In-memory lists:** `accounts`, `expenses`, `income`, `budgets`, `categories`, `goals`
- **Serialization:** Jackson `ObjectMapper` with `JavaTimeModule` and lenient deserialization (`FAIL_ON_UNKNOWN_PROPERTIES = false`)
- **Seeds default categories:** `"Food"`, `"Transport"`, `"Salary"` when the file is first created
- All mutation methods (`save`, `insert`) persist to disk immediately after modifying the in-memory list

---

### Authentication (`model.authentication`)

Uses a **Strategy** + **Template Method** pattern:

```
Auth (interface)
  └── AppAuth (concrete strategy, LocalStorage-backed)

Recognition (abstract template method)
  ├── Login    (validate credentials)
  └── SignUp   (validate new account and persist)
```

#### Flow

1. Controller creates an `Account` object from form fields
2. Calls `authEngine.perform(acc)` where `authEngine` is a `Login` or `SignUp` instance
3. `Recognition.perform()` calls abstract `validate(account)` → generates session token → calls `redirection()`
4. On success, `ApplicationState.initializeSession(email)` broadcasts the token to all managers

#### `Recognition` (Template Method)

- `perform(Account)` — validates, generates token (`tok_<email>_<timestamp>`), saves via `Session`, calls `ApplicationState.initializeSession()`, returns `SUCCESS`
- Abstract `validate(Account)` — implemented by subclasses
- Abstract `redirection(Account)` — currently both return `SUCCESS`

#### `Login`

- Delegates to `AppAuth.checkAgainstDataBase()` which validates email format (must contain `@` and a domain with `.`) and checks password match against stored accounts

#### `SignUp`

- Validates: email contains `@`, password ≥ 8 characters + alphanumeric, balance ≥ 0
- Checks email uniqueness via `AppAuth.emailExists()`
- Persists new account via `LocalStorage.insert(ACCOUNTS, account)`

#### `Session` (Singleton)

- Persists the session token encrypted to `session.dat` using AES
- Loads from file on first `getToken()` call (lazy initialization)
- Fires `TOKEN_CHANGED` property change events to notify observers on login/logout

#### `EncryptionUtil`

- AES encryption with a hardcoded key `"DuckSecurityKey1"`
- `encrypt(String)` → Base64-encoded ciphertext
- `decrypt(String)` → plaintext from Base64 input

---

### Account Operations (`model.accountOps`)

#### `AccountManager`

Manages the currently authenticated account.

| Method | Description |
| ------ | ----------- |
| `editAccount(Account, AccountConfig)` | Update theme, currency, avatar preferences |
| `updatePassword(Account, old, new)` | Validate old password, save new |
| `deleteAccount(Account)` | Remove account + all associated transactions, budgets, goals; clear session |
| `updateAccountName(Account, name)` | Change display name |

All mutating methods validate session token ownership by extracting the email from the token.

#### `ReportGenerator` (Interface)

`ArrayList<ReportConfig> generate(TransactionConfig config)`

#### `PDFReport`

Implements `ReportGenerator` using Apache PDFBox.

- Filters income/expense transactions by the given `TransactionConfig`
- Aggregates amounts by category
- Calculates percentage distribution
- Exports to `Documents/Reports/TransactionReport.pdf`
- Creates the directory if it doesn't exist

---

## Application State

`ApplicationState` is a **singleton** that owns all managers and coordinates event flow:

```java
public class ApplicationState {
    private static ApplicationState instance;
    private LocalStorage storage;
    private TransactionManager transactionManager;
    private BudgetManager budgetManager;
    private SavingGoalsManager goalsManager;
    private AccountManager accountManager;
}
```

**Initialization order:**
1. Gets `LocalStorage` singleton
2. Creates all four managers
3. Wires internal listeners: `budgetManager` and `goalsManager` listen to `transactionManager`
4. Wires alert listeners: `ApplicationState` listens to `budgetManager` and `goalsManager`
5. If an existing session token is found on disk, broadcasts it to all managers to restore state

**Key methods:**

| Method | Description |
| ------ | ----------- |
| `initializeSession(email)` | Creates session token (`<timestamp>_<email>`), persists via `Session`, broadcasts to managers |
| `clearSession()` | Saves null token via `Session`, broadcasts null to all managers |
| `getCurrentAccount()` | Extracts email from session token, looks up matching `Account` in storage |
| `addTransactionListener(PropertyChangeListener)` | Registers external listeners for transaction events |

**Alert Dialogs** (dispatched on JavaFX thread via `Platform.runLater`):
- `BUDGET_EXCEEDED` → Warning alert ("Budget Exceeded!")
- `THRESHOLD_REACHED` → Warning alert ("Budget Threshold Reached")
- `GOAL_COMPLETED` → Information alert with congratulations

---

## Event System

All events use `java.beans.PropertyChangeSupport` / `PropertyChangeListener` (not JavaFX properties).

### Event Flow Diagram

```
Session (TOKEN_CHANGED)
  ├── AccountManager       → reloads account data
  ├── TransactionManager   → loads/clears transactions
  ├── BudgetManager        → loads/clears budgets
  └── SavingGoalsManager   → loads/clears goals

TransactionManager (TRANSACTION_RECEIVED)
  ├── BudgetManager        → updates usedAmount per category
  │     └── fires BUDGET_EXCEEDED / THRESHOLD_REACHED
  │           └── ApplicationState → JavaFX Alert
  ├── SavingGoalsManager   → adds to active goal currentAmount
  │     └── fires GOAL_COMPLETED
  │           └── ApplicationState → JavaFX Alert
  ├── DashboardController  → refreshes dashboard UI
  └── ReportsController    → refreshes reports UI
```

---

## Theming

Two CSS files provide light and dark themes:

| File | Lines | Description |
| ---- | ----- | ----------- |
| `styles.css` | ~1558 | Dark theme (default) — charcoal backgrounds (`#121415`, `#1A1D21`), green accent (`#10B981`), white text |
| `theme-light.css` | ~397 | Light mode overrides — white/light gray backgrounds, dark text (`#111827`) |

Theme switching is handled by `App.setTheme(Mode)` which adds/removes `theme-light.css` from the active `Scene`. Each controller reads `account.getAccountConfig().getMode()` on initialization and applies the appropriate theme. Theme preference is persisted via `AccountManager.editAccount()`.

---

## Helper Utilities

### `CurrencyUtil`

- **Rates:** `USD_TO_EUR = 1/1.18`, `USD_TO_EGP = 1/0.019`
- **`getSymbol(Currency)`** — returns `$`, `EUR`, or `EGP `
- **`convertFromUsd(float, Currency)`** — converts USD to target
- **`convertToUsd(float, Currency)`** — converts from source to USD
- **`format(float, Currency)`** — symbol + 2 decimal places
- **`formatInt(float, Currency)`** — symbol + 0 decimal places

### `DialogHelper`

Builds modal JavaFX `Dialog` instances:

| Method | Input | Fields |
| ------ | ----- | ------ |
| `showTransactionDialog(Account, TransactionType)` | Account + type | Type toggle, category combo (editable), amount, date picker |
| `showBudgetDialog(Account)` | Account | Category, amount, start/end dates, threshold |
| `showGoalDialog(Account)` | Account | Name, target/current amounts, deadline |
| `showChangePasswordDialog()` | — | Old password, new password, confirm password |
| `showEditProfileDialog(Account)` | Account | Name field, avatar preview + FileChooser |
| `showNewCategoryDialog()` | — | Category name text input |

All dialogs include styled buttons (`-fx-button`) and theme stylesheets.

### `AvatarHelper`

- **`setSidebarAvatar(StackPane, Account)`** — loads the avatar image from the account's config path, clips it to a circle (radius 24px), and places it in the sidebar container; falls back to a default SVG person silhouette if no image is set

---

## Build & Run

### Prerequisites

- JDK 11
- Apache Maven

### Commands

```bash
# Build the project
mvn clean package

# Run the application
mvn clean javafx:run
```

### Entry Point

`com.duck.App.main()` — initializes JavaFX with a 960×720 window. Scene transitions use a 300ms fade + slide animation via `App.animateSceneTransition()`.

### Data Persistence

All data is stored in a single JSON file at the project root:

```
local_storage.json
```

Session state is stored in an encrypted file:

```
session.dat
```

---

## JavaDoc

```bash
mvn javadoc:javadoc
```

Generated documentation appears under `target/site/apidocs/`. Pre-generated JavaDoc is also available at `docs/apidocs/`.

---

## CS251 Submission Materials

The directory `CS251-2026-19-Sara-20240354-20240481-20240671-20240376/` contains course submission materials:

| Artifact | Description |
| -------- | ----------- |
| `Opportunities.pdf` | Project opportunities document |
| `Presentation.pptx` | Project presentation slides |
| `SDS.pdf` | Software Design Specification document |
| `implementation/` | Copy of the project source code with its own `docs/apidocs/` (generated JavaDoc) |

---

## Design Patterns Used

| Pattern | Where |
| ------- | ----- |
| **Singleton** | `ApplicationState`, `LocalStorage`, `Session` |
| **Strategy** | `StorageStrategy` / `LocalStorage`; `Auth` / `AppAuth` |
| **Template Method** | `Recognition` (abstract) / `Login`, `SignUp` (concrete) |
| **Observer** | `PropertyChangeListener` / `PropertyChangeSupport` across all managers and controllers |
| **MVC** | FXML (View) — Controller — Model layering |
