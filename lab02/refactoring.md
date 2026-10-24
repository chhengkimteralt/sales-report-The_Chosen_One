# Lab-02 · Task 4 Refactoring Report

## 1. Smells in `FatReportController` and Their Consequences

| # | Code Smell | Consequence |
|---|---|---|
| 1 | **I/O inside UI event handler** (`Files.list`, `Files.readAllLines`) | Blocks the JavaFX Application Thread, freezing the UI on slow disk or network drives. Untestable without actual file system state. |
| 2 | **Resource leak (unclosed stream)**: `Files.list(dir).toList()` without `try-with-resources` | `Files.list()` opens an underlying directory stream that must be closed. On Windows, this keeps directory handles open, blocking subsequent file ops or deletions. |
| 3 | **No row validation & incorrect revenue calculation** | Does not check for required 10 fields, negative quantities, negative prices, or discounts exceeding gross line amount. As a result, corrupt data (e.g., negative quantity `-1`) silently corrupts the aggregated revenue figures rather than being rejected and logged. |
| 4 | **No precision handling (arbitrary scale BigDecimal arithmetic)** | Uses raw `new BigDecimal(...)` without setting scale (2) or rounding mode (`RoundingMode.HALF_UP`). Violates Angkor Mart accounting rules. |
| 5 | **Swallowing exceptions with generic error** (`catch (Exception e) { status.setText("Error"); }`) | Completely hides root cause (e.g., `NoSuchFileException`, `NumberFormatException`, `DateTimeParseException`), leaving users and developers blind to what failed. |
| 6 | **Tight coupling to JavaFX UI widgets** | The handler reads `monthField.getText()` and `branchBox.getValue()` inside nested iteration, preventing any headless testing or code reuse. |

---

## 2. Comparison: Fat Controller vs After Refactoring

| Metric / Scenario | Fat Controller | After Refactoring |
|---|---|---|
| **2026-09 ALL, broken row present** (`quantity = -1`, `price = 6.50`) | Revenue: `26715.40` (silently subtracts 6.50; wrong, no warning) | Revenue: `USD 26721.90 · 1 rejected` (correctly ignored and counted) |
| **Month `2026/09`** | Shows generic `"Error"` | Shows clear validation error: `"'2026/09' is not a month in the form yyyy-MM"` |
| **Lines in `onLoad()` handler** | 20 lines | 2 lines (delegating to controller) |
| **Tests possible without JavaFX** | 0 tests | 26 tests (model, controller, input rules, parser) |
