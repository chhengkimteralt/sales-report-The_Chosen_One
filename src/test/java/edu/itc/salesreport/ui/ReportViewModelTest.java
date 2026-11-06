package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Plain JUnit: no JavaFX toolkit, no window, no robot, no sleep. */
class ReportViewModelTest {

    static final Clock SEP_27 = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"),
            ZoneId.of("Asia/Phnom_Penh"));
    static final YearMonth AUG = YearMonth.of(2026, 8);

    ManualJobs jobs = new ManualJobs();
    List<String> loaded = new ArrayList<>();
    boolean failNext;
    String revenue = "200.00";
    ReportViewModel vm;

    @BeforeEach
    void setUp() {
        SummarySource source = (month, branch) -> {
            if (failNext) {
                throw new IOException(branch + "-2026-08-03.csv: unreadable");
            }
            loaded.add(branch);
            return summary(branch, revenue);
        };
        vm = new ReportViewModel(source, jobs, SEP_27);
    }

    static BranchSummary summary(String branch, String revenue) {
        var rice = new ProductTotal("SKU-1001", "Jasmine Rice 5kg",
                new BigDecimal(revenue), 5);
        return new BranchSummary(branch, new BigDecimal(revenue),
                BigDecimal.ZERO, 10, Map.of(), List.of(rice), Map.of());
    }

    private void loadOnce() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
    }

    // ---- validation (3)

    @Test
    void futureMonthIsRejected() {
        vm.monthProperty().set(YearMonth.of(2026, 10));
        assertEquals("Month 2026-10 is in the future.",
                vm.monthErrorProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void cannotGenerateBeforeAMonthIsChosen() {
        assertEquals("Choose a month.", vm.validationMessageProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void emptyBranchSelectionIsRejected() {
        vm.monthProperty().set(AUG);
        for (String b : ReportViewModel.BRANCHES) {
            vm.branchSelectedProperty(b).set(false);
        }
        assertEquals("Select at least one branch.",
                vm.validationMessageProperty().get());
        assertFalse(vm.canGenerateProperty().get());

        vm.monthProperty().set(null);
        assertEquals("Choose a month. Select at least one branch.",
                vm.validationMessageProperty().get());

        vm.monthProperty().set(YearMonth.of(2026, 9));
        vm.branchSelectedProperty("PNH").set(true);
        assertEquals("", vm.validationMessageProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
    }

    // ---- command enablement (2)

    @Test
    void generateStartsLoadingAndDisablesItself() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        assertTrue(vm.loadingProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
        assertTrue(vm.cancelCommand().isExecutable());
        assertEquals(1, jobs.pendingCount());
    }

    @Test
    void generateIsIgnoredWhileInvalid() {
        vm.generateCommand().execute(); // no month yet
        assertEquals(0, jobs.pendingCount());
        assertFalse(vm.loadingProperty().get());
        assertFalse(vm.cancelCommand().isExecutable());
    }

    // ---- success, progress, failure, cancellation, filter

    @Test
    void successfulLoadFillsSummariesAndTotal() {
        revenue = "1234.50";
        loadOnce();
        assertEquals(3, vm.summaries().size());
        assertEquals(new BigDecimal("3703.50"), vm.totalRevenueProperty().get());
        assertEquals("USD 3,703.50", vm.totalRevenueTextProperty().get());
        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
        assertFalse(vm.cancelCommand().isExecutable());
    }

    @Test
    void progressFollowsTheBranches() {
        List<Double> seen = new ArrayList<>();
        vm.progressProperty().addListener((o, a, b) -> seen.add(b.doubleValue()));
        loadOnce();
        assertEquals(3, seen.size());
        assertEquals(1.0 / 3, seen.get(0), 1e-9);
        assertEquals(2.0 / 3, seen.get(1), 1e-9);
        assertEquals(1.0, seen.get(2), 1e-9);
    }

    @Test
    void failureKeepsPreviousDataAndShowsTheError() {
        loadOnce();
        failNext = true;
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(3, vm.summaries().size());
        assertEquals("USD 600.00", vm.totalRevenueTextProperty().get());
        assertEquals("PNH-2026-08-03.csv: unreadable",
                vm.loadErrorProperty().get());
        assertEquals("Load failed, previous data kept.",
                vm.statusProperty().get());
        assertFalse(vm.loadingProperty().get());
    }

    @Test
    void cancelReenablesGenerateAndKeepsOldData() {
        loadOnce();
        vm.generateCommand().execute();
        assertTrue(vm.loadingProperty().get());
        vm.cancelCommand().execute();
        assertFalse(vm.loadingProperty().get());
        assertEquals("Cancelled, previous data kept.", vm.statusProperty().get());
        assertEquals(3, vm.summaries().size());
        assertTrue(vm.generateCommand().isExecutable());
        assertEquals(0, jobs.pendingCount());
    }

    @Test
    void productFilterIsCaseInsensitiveOnNameAndSku() {
        loadOnce();
        assertEquals(1, vm.visibleProducts().size());
        vm.productFilterProperty().set("JASMINE");
        assertEquals(1, vm.visibleProducts().size());
        vm.productFilterProperty().set("sku-1001");
        assertEquals(1, vm.visibleProducts().size());
        vm.productFilterProperty().set("nothing like this");
        assertTrue(vm.visibleProducts().isEmpty());
        vm.productFilterProperty().set("");
        assertEquals(1, vm.visibleProducts().size());
    }

    @Test
    void workerUsesASnapshotOfTheInputs() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        // user keeps editing while the job is queued
        vm.branchSelectedProperty("BTB").set(false);
        vm.monthProperty().set(YearMonth.of(2026, 7));
        jobs.runNext();
        assertEquals(List.of("PNH", "REP", "BTB"), loaded);
        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
    }
}
