# lab02/mvc-vs-mvp.md

| Criterion | MVC (ReportView + ReportController) | MVP (FxReportScreen + ReportPresenter) |
|---|---|---|
| Lines of code (view / controller-presenter) | View: 115 lines (`ReportView.java`)<br>Controller: 51 lines (`ReportController.java`)<br>**Total: 166 lines** | View: 103 lines (`FxReportScreen.java`)<br>Presenter: 92 lines (`ReportPresenter.java`)<br>Contract: 29 lines (`ReportScreen.java` + `RowItem.java`)<br>**Total: 224 lines** |
| Tests that run without JavaFX | **4 tests** (`ReportControllerTest`: invalid month, valid month, missing folder, branch filter) | **6 tests** (`ReportControllerTest` 4 + `ReportPresenterTest` 2: status formatting, table row formatting, money formatting) |
| Where money and status text are formatted | View (`ReportView`: formats string labels, table cells, and status in `modelChanged`) | Presenter (`ReportPresenter`: formats status, basket, and `RowItem` strings before passing to passive view) |
| Files changed to add a "Discount" column | **1 file**: `ReportView.java` (added column binding to `SaleTransaction::discount`) | **3 files**: `RowItem.java` (record field), `ReportPresenter.java` (mapped `tx.discount()`), `FxReportScreen.java` (table column) |
| Does the view hold a model reference? | **Yes** (`ReportView` holds `SalesModel` and registers as listener) | **No** (`FxReportScreen` only depends on `ReportScreen` contract and `ReportPresenter`) |
| Effort for a second screen (web, Swing) | **Medium-High**: New view must query `SalesModel`, listen to `SalesEvent`, and re-implement formatting rules | **Low**: Presenter handles all business and formatting logic; new view only implements `ReportScreen` passive interface |
