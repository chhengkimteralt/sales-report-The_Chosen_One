package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.SimpleReportEngine;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Benchmark: CallerThreadJobs vs FxBackgroundJobs.
 * mvn -q javafx:run -Dapp.main=edu.itc.salesreport.ui.LoadBenchmarkApp
 *     "-Djavafx.args=--data=data --month=2026-08"
 * 2 warm-up + 5 measured runs per mode; prints load ms and worst frame gap.
 */
public final class LoadBenchmarkApp extends Application {

    private static final int WARMUP = 2;
    private static final int MEASURED = 5;
    private static final Duration SETTLE = Duration.millis(300);

    private record Mode(String name, Supplier<BackgroundJobs> jobs) { }
    private record Result(String mode, int run, long loadMs, double gapMs) { }

    private final FrameGapMonitor monitor = new FrameGapMonitor();
    private final List<Result> results = new ArrayList<>();
    private ExecutorService workers;
    private SummarySource source;
    private YearMonth month;
    private List<Mode> modes;
    private int modeIndex;
    private int runIndex; // 0 .. WARMUP+MEASURED-1

    @Override
    public void start(Stage stage) {
        Path dataDir = Path.of(getParameters().getNamed().getOrDefault("data", "data"));
        month = YearMonth.parse(getParameters().getNamed().getOrDefault("month", "2026-08"));
        var engine = new SimpleReportEngine();
        source = (m, b) -> engine.summarizeBranch(m, b, dataDir);
        workers = Executors.newVirtualThreadPerTaskExecutor();
        modes = List.of(
                new Mode("caller-thread", CallerThreadJobs::new),
                new Mode("fx-task", () -> new FxBackgroundJobs(workers)));

        stage.setScene(new Scene(new Label("Benchmark running, see the console..."), 360, 80));
        stage.setTitle("Load benchmark");
        stage.show();
        monitor.start();
        System.out.printf("%-14s %4s %8s %18s%n", "mode", "run", "load ms", "max frame gap ms");
        Platform.runLater(this::nextRun);
    }

    private void nextRun() {
        if (modeIndex >= modes.size()) {
            finish();
            return;
        }
        Mode mode = modes.get(modeIndex);
        var vm = new ReportViewModel(source, mode.jobs().get(), Clock.systemDefaultZone());
        vm.monthProperty().set(month);

        long[] startNanos = {0};
        long[] loadMs = {-1};
        vm.loadingProperty().addListener((obs, was, is) -> {
            if (was && !is) {
                loadMs[0] = (System.nanoTime() - startNanos[0]) / 1_000_000;
            }
        });

        monitor.reset();
        startNanos[0] = System.nanoTime();
        vm.generateCommand().execute(); // caller-thread: blocks the FX thread here

        // let the FX thread render a few frames, then read the worst gap
        PauseTransition settle = new PauseTransition(SETTLE);
        settle.setOnFinished(e -> {
            if (loadMs[0] < 0) {          // fx-task: still loading, wait more
                settle.playFromStart();
                return;
            }
            record(mode, loadMs[0], monitor.maxGapMillis());
            if (++runIndex == WARMUP + MEASURED) {
                runIndex = 0;
                modeIndex++;
            }
            Platform.runLater(this::nextRun);
        });
        settle.play();
    }

    private void record(Mode mode, long loadMs, double gapMs) {
        if (runIndex < WARMUP) {
            return; // warm-up runs are not reported
        }
        int run = runIndex - WARMUP + 1;
        results.add(new Result(mode.name(), run, loadMs, gapMs));
        System.out.printf("%-14s %4d %8d %18.0f%n", mode.name(), run, loadMs, gapMs);
    }

    private void finish() {
        for (Mode mode : modes) {
            var rs = results.stream().filter(r -> r.mode().equals(mode.name())).toList();
            double mean = rs.stream().mapToLong(Result::loadMs).average().orElse(0);
            double worst = rs.stream().mapToDouble(Result::gapMs).max().orElse(0);
            System.out.printf("%-13s mean load %6.0f ms, worst frame gap %5.0f ms%n",
                    mode.name(), mean, worst);
        }
        Platform.exit();
    }

    @Override
    public void stop() {
        monitor.stop();
        workers.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
