# ADR-002: Selection of Presentation Pattern (MVC vs MVP)

* **Status:** Accepted
* **Date:** 2026-10-24
* **Deciders:** The Chosen One Team

## Context

Angkor Mart requires a desktop monthly sales report viewer in JavaFX, with potential upcoming extensions for web (Spring MVC) and command-line interfaces.
In Lab-02, we implemented and evaluated two architectural variants for the user interface:
1. **Model-View-Controller (MVC):** The view observes `SalesModel` directly via `SalesModelListener`, queries `visibleTransactions()` and `summary()`, and formats data internally for display. The controller validates raw user gestures into `ReportCommand`s and modifies the model.
2. **Model-View-Presenter (MVP - Passive View):** The view (`ReportScreen`) is completely passive and has zero reference to `SalesModel`. The presenter observes `SalesModel`, converts transactions to formatted string records (`RowItem`), formats summary metrics (money, average basket), and pushes them into the view interface.

### Quality-Attribute Scenario (from Lab-01)
* **Testability Scenario:** *A UI presentation logic change (status formatting, monetary display, error handling) must be unit-testable in automated CI in under 1 second without launching a display or UI toolkit.*
* **Modifiability Scenario:** *Adding a secondary client (e.g. CLI or Web) or migrating to JavaFX data bindings (MVVM in Lab-03) should not require rewriting business and aggregation rules.*

## Decision

We retain **MVC** for the baseline desktop application while adopting **MVP's separation principles** in preparation for the transition to **MVVM (Model-View-ViewModel)** in Lab-03.

For the immediate Lab-02 architecture:
* MVC requires fewer intermediate data transfer structures (`RowItem`), avoids double-binding boilerplate, and directly leverages observable model events.
* In Lab-03, JavaFX properties and observable bindings will naturally bridge the view and presentation layer without needing MVP's explicit passive setters.

## Consequences

### Positive
* **Simplicity & Maintainability:** Avoids redundant record definitions (`RowItem`) and extra interface layers across simple screens.
* **Direct UI Toolkit Integration:** MVC view components can directly bind to model collections.
* **Clear Migration Path to MVVM:** The observable `SalesModel` and command-based controller map cleanly to ViewModels in Lab-03.

### Negative
* Testing view-specific formatting in MVC requires either UI tests or testing components that couple to UI widgets, whereas MVP allows 100% headless testing of all formatted strings via a simple `FakeScreen`.
