# Lab-03: MVC vs MVVM (our own code)

MVC side = Lab-02 `ui/fx/ReportView` + `ui/ReportController`. MVVM side = Lab-03 `ui/*`.

| Metric | MVC (Lab-02) | MVVM (Lab-03) | How measured |
|---|---|---|---|
| Lines of presentation code | 50 controller + 115 view = 165 | 293 ViewModel + 126 view + 164 ports, commands, jobs (`Command`, `DelegateCommand`, `SummarySource`, `BackgroundJobs`, `CallerThreadJobs`, `FxBackgroundJobs`) | `wc -l` |
| Presentation-logic line coverage without toolkit | <fill>% (`ReportController` row, only the 4 controller tests; logic in `fx/ReportView` is not testable without JavaFX) | <fill>% (`ReportViewModel` row) | JaCoCo 0.8.15, `target/site/jacoco/jacoco.csv` |
| `javafx.scene` imports in the logic class | 0 in `ReportController` (9 in the view, which holds `modelChanged` formatting) | 0 in `ReportViewModel` (13 in the view, bindings only) | `grep -c "^import javafx.scene"` |
| Control types the logic class references | 0 | 0 | code review |
| Test run time of presentation logic | <fill> s | <fill> s for 11 tests | Surefire report |
| UI frozen during a load | yes (worst gap <fill> ms) | no (<fill> ms) | Task 4, `measurements.md` |

## Conclusion
<write 5 to 8 sentences in your own words. Points that hold for our code:
- Better: the rules (validation, canGenerate, totals, filter, cancel) now live in one class that tests with plain JUnit, ManualJobs and a fixed Clock, no sleeps; the view has no `if`; the UI no longer freezes because loading goes through the BackgroundJobs port.
- Better: our Lab-02 MVC controller was already toolkit-free, but formatting and enable/disable logic sat in the view (`modelChanged`), so that part was untestable; MVVM moved it out.
- Worse: more classes and about 2.5x more lines (ports, commands, jobs); binding bugs are harder to debug than an explicit setText; threading is a new failure source.
- Still choose MVC for: a tiny screen with little state, one view, no background work.>
