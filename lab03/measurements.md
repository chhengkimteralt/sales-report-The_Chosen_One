# Task 4: Background loading measurements

## Method
- Data: August 2026, 5,000 rows per branch and day, seed 42 (93 files, 465,000 rows)
  `mvn -q compile` then `java -cp target/classes edu.itc.salesreport.ingest.SalesDataGenerator 2026-08 5000 42 data`
- Harness: `LoadBenchmarkApp` (2 warm-up + 5 measured runs per mode, fresh ViewModel per run), `FrameGapMonitor` = longest gap between two FX pulses (idle ~16.7 ms).
- Command: `mvn -q javafx:run -Dapp.main=edu.itc.salesreport.ui.LoadBenchmarkApp "-Djavafx.args=--data=data --month=2026-08"`

## Hardware / versions
| CPU | RAM | OS | JDK | JavaFX |
|---|---|---|---|---|
| <fill> | <fill> GB | <fill> | 25.0.x | 25.0.4 |

## Runs (paste the console output)
| mode | run | load ms | max frame gap ms |
|---|---|---|---|
| caller-thread | 1 | <fill> | <fill> |
| caller-thread | 2 | <fill> | <fill> |
| caller-thread | 3 | <fill> | <fill> |
| caller-thread | 4 | <fill> | <fill> |
| caller-thread | 5 | <fill> | <fill> |
| fx-task | 1 | <fill> | <fill> |
| fx-task | 2 | <fill> | <fill> |
| fx-task | 3 | <fill> | <fill> |
| fx-task | 4 | <fill> | <fill> |
| fx-task | 5 | <fill> | <fill> |

| mode | mean load ms | worst frame gap ms |
|---|---|---|
| caller-thread | <fill> | <fill> |
| fx-task | <fill> | <fill> (must be < 100) |

## Manual observation (20,000 rows per day)
Regenerate: `java -cp target/classes edu.itc.salesreport.ingest.SalesDataGenerator 2026-08 20000 42 data20k`, run with `--data=data20k`.

<write 3 to 5 sentences: with CallerThreadJobs the window does not repaint, typing in the filter and moving the window stall and Cancel cannot be pressed; with FxBackgroundJobs the progress bar advances per branch, typing and moving work, Cancel stops the load and the old table stays. Use your own words and what you actually saw.>
