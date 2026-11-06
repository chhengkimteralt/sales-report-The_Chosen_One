# Lab-03 Deliverables & Change Log

**Team:** The Chosen One | **Chapter 03:** MVVM | **Project:** `sales-report`

## Files of this lab
| Path | Purpose | Author |
|---|---|---|
| `src/main/java/edu/itc/salesreport/ui/Command.java`, `DelegateCommand.java` | Command abstraction with observable "can run" flag | <fill> |
| `ui/SummarySource.java`, `ui/BackgroundJobs.java` | Ports: model access and threading | <fill> |
| `ui/ReportViewModel.java` | State, validation, commands, loading (javafx.base only) | <fill> |
| `ui/ReportView.java`, `ui/ReportViewerApp.java` | Binding-only view, composition root | <fill> |
| `ui/CallerThreadJobs.java` | Synchronous jobs (frozen-UI baseline) | <fill> |
| `ui/FxBackgroundJobs.java` | `javafx.concurrent.Task` adapter, progress and cancel | <fill> |
| `ui/FrameGapMonitor.java`, `ui/LoadBenchmarkApp.java` | UI responsiveness benchmark | <fill> |
| `src/test/.../ui/ManualJobs.java` | Test double, jobs run on demand | <fill> |
| `src/test/.../ui/ReportViewModelTest.java` | 11 ViewModel tests, no JavaFX toolkit | <fill> |
| `src/test/.../ui/ViewModelImportsTest.java` | Architecture guard for imports | <fill> |
| `lab03/viewmodel-design.md` | Inventory, class and state diagrams | <fill> |
| `lab03/measurements.md` | Task 4 measurements | <fill> |
| `lab03/mvc-vs-mvvm.md` | Task 5 comparison | <fill> |
| `lab03/screenshots/viewer.png` | Screenshot of the MVVM viewer | <fill> |

## Inputs reused from Lab-02
| Input | Source |
|---|---|
| Maven project, model, ingest, engine packages | tag `lab-02` (commit d96abe2), unchanged |
| Lab-02 MVC viewer (`ui/fx/ReportView`, `ReportController`) | tag `lab-02`, kept for the Task 5 comparison |
| Sample data | generated with `SalesDataGenerator` (Task 4, seed 42) |

## Change log
| Date | What changed | Who | Model package changed? |
|---|---|---|---|
| 2026-11-05 | Tasks 0-3: ports, ViewModel, binding view, validation | Kimter, Mengly | No |
| 2026-11-06 | Task 4: FxBackgroundJobs, benchmark, cancel via CancellationException, status "Cancelled, previous data kept." | <fill> | No |
| 2026-11-06 | Task 5: 11 ViewModel tests with ManualJobs, imports guard, docs | <fill> | No |
