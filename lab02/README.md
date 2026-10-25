# Lab-02 Deliverables & Change Log

## Team & Project Information
* **Team:** The Chosen One
* **Course:** Software Engineering Concepts (Chapter 02 - MVC / MVP)
* **Project:** Angkor Mart Monthly Sales Report (`sales-report`)

---

## Lab-01 Inputs Reused

| Input Artifact | Origin / Commit | Notes |
|---|---|---|
| Team Repository & Roles | Initialized repository | Collaborators configured on GitHub |
| Quality-Attribute Scenarios | `lab01` (Fallback used) | Testability: Headless testing of presentation logic under 1s; Modifiability: Adding new views without touching parsing |
| C4 Container Diagram | `lab01` (Fallback used) | Container: "Report viewer (JavaFX desktop)" |
| ADR Template | `lab01` (Fallback used) | Format: Title, Status, Context, Decision, Consequences |

---

## Lab-02 File Index

| File Path | Task | Purpose / Description |
|---|---|---|
| `src/main/java/edu/itc/salesreport/model/*` | Task 0-1 | Domain records (`SaleTransaction`, `BranchSummary`, `ProductTotal`, `PaymentMethod`) |
| `src/main/java/edu/itc/salesreport/ingest/*` | Task 1 | CSV parser and reader (`CsvTransactionParser`, `CsvMonthReader`, `MonthData`, `InvalidRowException`) |
| `src/main/java/edu/itc/salesreport/engine/*` | Task 1 | Aggregation engine (`SalesAggregator`, `DefaultSalesAggregator`) |
| `src/main/java/edu/itc/salesreport/ui/SalesModel.java` | Task 1 | Observable, headless sales model with listener notification |
| `src/main/java/edu/itc/salesreport/ui/console/ConsoleReportView.java` | Task 1 | Toolkit-free console view |
| `src/main/java/edu/itc/salesreport/ui/fx/ReportView.java` | Task 2, 5 | JavaFX MVC view with table, category bar chart, and Discount column |
| `src/main/java/edu/itc/salesreport/ui/fx/ReportApp.java` | Task 2-3 | JavaFX MVC Application launcher |
| `src/main/java/edu/itc/salesreport/ui/InputRules.java` | Task 3 | Pure input validation rules for month and branch |
| `src/main/java/edu/itc/salesreport/ui/Validation.java` | Task 3 | Sealed validation result interface (`Valid`, `Invalid`) |
| `src/main/java/edu/itc/salesreport/ui/ReportCommand.java` | Task 3 | Sealed commands (`LoadMonth`, `SelectBranch`) |
| `src/main/java/edu/itc/salesreport/ui/ReportController.java` | Task 3 | MVC Controller validating input and updating model |
| `src/main/java/edu/itc/salesreport/ui/ErrorDisplay.java` | Task 3 | Error display interface |
| `src/test/java/edu/itc/salesreport/ingest/CsvTransactionParserTest.java` | Task 4 | Unit tests for CSV parser including parameterized invalid row tests |
| `src/test/java/edu/itc/salesreport/ui/InputRulesTest.java` | Task 4 | Unit tests for input validation with fixed clock |
| `src/test/java/edu/itc/salesreport/ui/ReportControllerTest.java` | Task 4 | Headless controller tests using fake error display |
| `src/test/java/edu/itc/salesreport/UiIndependenceTest.java` | Task 4 | Architecture rule test ensuring no toolkit imports in core packages |
| `src/main/java/edu/itc/salesreport/ui/fx/legacy/FatReportController.java` | Task 4 | Refactored legacy fat controller delegating to `ReportController` |
| `lab02/refactoring.md` | Task 4 | Documentation of fat controller code smells and refactoring results |
| `src/main/java/edu/itc/salesreport/ui/mvp/RowItem.java` | Task 5 | MVP formatted row item record |
| `src/main/java/edu/itc/salesreport/ui/mvp/ReportScreen.java` | Task 5 | MVP passive view interface |
| `src/main/java/edu/itc/salesreport/ui/mvp/ReportPresenter.java` | Task 5 | MVP Presenter handling formatting, gestures, and model notifications |
| `src/main/java/edu/itc/salesreport/ui/fx/FxReportScreen.java` | Task 5 | Passive JavaFX screen implementation |
| `src/main/java/edu/itc/salesreport/ui/fx/ReportMvpApp.java` | Task 5 | JavaFX MVP Application launcher |
| `src/test/java/edu/itc/salesreport/ui/mvp/ReportPresenterTest.java` | Task 5 | Headless tests verifying exact formatted display values |
| `lab02/mvc-vs-mvp.md` | Task 5 | Detailed measurement and comparison table between MVC and MVP |
| `lab02/adr-002-presentation-pattern.md` | Task 5 | Architecture Decision Record choosing MVC/MVVM direction |
| `sales-web/pom.xml` | Challenge | Spring MVC project POM with Spring Boot 4.1.1 and Thymeleaf |
| `sales-web/src/main/java/edu/itc/salesreport/web/SalesWebApp.java` | Challenge | Spring Boot application declaring beans for CsvMonthReader, SalesAggregator, and Clock |
| `sales-web/src/main/java/edu/itc/salesreport/web/ReportWebController.java` | Challenge | Spring MVC controller validating inputs and querying SalesModel |
| `sales-web/src/main/resources/templates/report.html` | Challenge | Thymeleaf template rendering monthly report, branch selector, error message, and totals |
| `sales-web/src/test/java/edu/itc/salesreport/web/ReportWebControllerTest.java` | Challenge | @WebMvcTest testing valid request, invalid months, and branch validation with MockMvcTester |

---

## Change Log

| Version / Tag | Date | Highlights |
|---|---|---|
| `lab-02` | 2026-10-25 | Completed Tasks 1-5 and Challenge (Extra Credit): Spring MVC web view project `sales-web` reusing domain and model unchanged. |
