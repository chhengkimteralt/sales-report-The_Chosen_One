package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.engine.SimpleReportEngine;
import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportViewModelTest {

    private SimpleReportEngine engine;
    private ReportViewModel vm;
    private final Path dataDir = Path.of("data");

    @BeforeEach
    void setUp() {
        engine = new SimpleReportEngine();
        SummarySource source = (month, branch) -> engine.summarizeBranch(month, branch, dataDir);
        Clock clock = Clock.fixed(Instant.parse("2026-09-01T00:00:00Z"), ZoneId.systemDefault());
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
    void testGenerateForAugust2026() {
        vm.monthProperty().set(YearMonth.of(2026, 8));
        assertTrue(vm.canGenerateProperty().get());

        vm.generateCommand().execute();

        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
        assertEquals(3, vm.summaries().size());

        for (BranchSummary summary : vm.summaries()) {
            System.out.println("Branch: " + summary.branch() + " Revenue: " + summary.revenue()
                    + " Discounts: " + summary.discounts() + " Receipts: " + summary.receipts()
                    + " AvgBasket: " + summary.averageBasket());
        }

        System.out.println("Total Revenue: " + vm.totalRevenueProperty().get());
        System.out.println("Total Revenue Text: " + vm.totalRevenueTextProperty().get());
        System.out.println("Merged Top Products count: " + vm.visibleProducts().size());
        for (ProductTotal pt : vm.visibleProducts()) {
            System.out.println("Product: " + pt.sku() + " " + pt.productName() + " " + pt.revenue() + " " + pt.quantity());
        }

        assertNotNull(vm.totalRevenueProperty().get());
        assertTrue(vm.totalRevenueProperty().get().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(vm.totalRevenueTextProperty().get().startsWith("USD "));
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
