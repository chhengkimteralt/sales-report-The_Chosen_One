package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.SimpleReportEngine;
import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class ReportViewModelTest {

    private SimpleReportEngine engine;
    private ReportViewModel vm;
    private final Path dataDir = Path.of("data");
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T00:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        engine = new SimpleReportEngine();
        SummarySource source = (month, branch) -> engine.summarizeBranch(month, branch, dataDir);
        vm = new ReportViewModel(source, new CallerThreadJobs(), clock);
    }

    @Test
    void testInitialState() {
        assertEquals("Choose a month.", vm.statusProperty().get());
        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.summaries().isEmpty());
        assertTrue(vm.visibleProducts().isEmpty());
        assertEquals("USD 0.00", vm.totalRevenueTextProperty().get());
    }

    @Test
    void testExpectedOutputFiveInputCombinations() {
        // Combination 1: Month = (empty), branches = PNH, REP, BTB
        vm.monthProperty().set(null);
        vm.branchSelectedProperty("PNH").set(true);
        vm.branchSelectedProperty("REP").set(true);
        vm.branchSelectedProperty("BTB").set(true);
        assertEquals("Choose a month.", vm.validationMessageProperty().get());
        assertEquals("Choose a month.", vm.monthErrorProperty().get());
        assertEquals("", vm.branchErrorProperty().get());
        assertFalse(vm.canGenerateProperty().get());
        assertFalse(vm.generateCommand().isExecutable());

        // Combination 2: Month = 2026-10, branches = PNH, REP, BTB
        vm.monthProperty().set(YearMonth.of(2026, 10));
        assertEquals("Month 2026-10 is in the future.", vm.validationMessageProperty().get());
        assertEquals("Month 2026-10 is in the future.", vm.monthErrorProperty().get());
        assertEquals("", vm.branchErrorProperty().get());
        assertFalse(vm.canGenerateProperty().get());
        assertFalse(vm.generateCommand().isExecutable());

        // Combination 3: Month = 2026-08, branches = none
        vm.monthProperty().set(YearMonth.of(2026, 8));
        vm.branchSelectedProperty("PNH").set(false);
        vm.branchSelectedProperty("REP").set(false);
        vm.branchSelectedProperty("BTB").set(false);
        assertEquals("Select at least one branch.", vm.validationMessageProperty().get());
        assertEquals("", vm.monthErrorProperty().get());
        assertEquals("Select at least one branch.", vm.branchErrorProperty().get());
        assertFalse(vm.canGenerateProperty().get());
        assertFalse(vm.generateCommand().isExecutable());

        // Combination 4: Month = (empty), branches = none
        vm.monthProperty().set(null);
        assertEquals("Choose a month. Select at least one branch.", vm.validationMessageProperty().get());
        assertEquals("Choose a month.", vm.monthErrorProperty().get());
        assertEquals("Select at least one branch.", vm.branchErrorProperty().get());
        assertFalse(vm.canGenerateProperty().get());
        assertFalse(vm.generateCommand().isExecutable());

        // Combination 5: Month = 2026-09, branches = PNH
        vm.monthProperty().set(YearMonth.of(2026, 9));
        vm.branchSelectedProperty("PNH").set(true);
        vm.branchSelectedProperty("REP").set(false);
        vm.branchSelectedProperty("BTB").set(false);
        assertEquals("", vm.validationMessageProperty().get());
        assertEquals("", vm.monthErrorProperty().get());
        assertEquals("", vm.branchErrorProperty().get());
        assertTrue(vm.canGenerateProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
    }

    @Test
    void testForcedLoadErrorKeepsPreviousData() {
        // First successful load
        vm.monthProperty().set(YearMonth.of(2026, 8));
        vm.generateCommand().execute();
        assertEquals(3, vm.summaries().size());
        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
        assertEquals("", vm.loadErrorProperty().get());

        // Create VM with failing source
        SummarySource failingSource = (month, branch) -> {
            if ("REP".equals(branch)) {
                throw new IOException("REP-2026-08-03.csv: unreadable");
            }
            return engine.summarizeBranch(month, branch, dataDir);
        };
        ReportViewModel failingVm = new ReportViewModel(failingSource, new CallerThreadJobs(), clock);
        // Copy loaded summaries to simulate previous data
        failingVm.monthProperty().set(YearMonth.of(2026, 8));

        // When load fails:
        failingVm.generateCommand().execute();
        assertEquals("REP-2026-08-03.csv: unreadable", failingVm.loadErrorProperty().get());
        assertEquals("Load failed, previous data kept.", failingVm.statusProperty().get());
        assertFalse(failingVm.loadingProperty().get());
    }

    @Test
    void testGenerateForAugust2026() {
        vm.monthProperty().set(YearMonth.of(2026, 8));
        assertTrue(vm.canGenerateProperty().get());

        vm.generateCommand().execute();

        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
        assertEquals(3, vm.summaries().size());

        assertNotNull(vm.totalRevenueProperty().get());
        assertTrue(vm.totalRevenueProperty().get().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("USD 26,452.70", vm.totalRevenueTextProperty().get());
    }

    @Test
    void testProductFilter() {
        vm.monthProperty().set(YearMonth.of(2026, 8));
        vm.generateCommand().execute();

        int initialCount = vm.visibleProducts().size();
        assertTrue(initialCount > 0);

        String firstProductName = vm.visibleProducts().get(0).productName();
        vm.productFilterProperty().set(firstProductName);
        assertFalse(vm.visibleProducts().isEmpty());
        for (ProductTotal pt : vm.visibleProducts()) {
            assertTrue(pt.productName().toLowerCase().contains(firstProductName.toLowerCase())
                    || pt.sku().toLowerCase().contains(firstProductName.toLowerCase()));
        }

        vm.productFilterProperty().set("NON_EXISTING_XYZ_123");
        assertTrue(vm.visibleProducts().isEmpty());

        vm.productFilterProperty().set("");
        assertEquals(initialCount, vm.visibleProducts().size());
    }
}
