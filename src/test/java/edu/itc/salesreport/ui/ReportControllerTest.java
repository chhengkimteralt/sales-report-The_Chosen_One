package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.*;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Test for the MVC controller without any JavaFX UI. */
class ReportControllerTest {
    static final class FakeErrors implements ErrorDisplay {
        final List<String> shown = new ArrayList<>();

        @Override
        public void showError(String message) {
            shown.add(message);
        }

        @Override
        public void clearError() {
            shown.add("<cleared>");
        }
    }

    @TempDir
    Path dataDir;
    private final FakeErrors errors = new FakeErrors();
    private final List<SalesEvent> events = new ArrayList<>();
    private SalesModel model;
    private ReportController controller;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(dataDir);
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        model = new SalesModel(reader, aggregator);
        model.addListener(events::add);
        Clock clock = Clock.fixed(Instant.parse("2026-09-27T03:00:00Z"), ZoneId.of("Asia/Phnom_Penh"));
        controller = new ReportController(model, dataDir, clock, errors);
    }

    @Test
    void invalidMonthNeverReachesModel() {
        controller.monthEntered("09/2026");
        assertTrue(events.isEmpty());
        assertEquals(List.of("'09/2026' is not a month in the form yyyy-MM"), errors.shown);
    }

    @Test
    void validMonthLoadsModelAndClearsError() {
        controller.monthEntered("2026-09");
        assertFalse(events.isEmpty());
        assertTrue(errors.shown.contains("<cleared>"));
        assertEquals(3, model.visibleTransactions().size());
    }

    @Test
    void missingFolderBecomesMessage() {
        controller.monthEntered("2026-07");
        assertTrue(events.isEmpty());
        assertTrue(errors.shown.stream().anyMatch(s -> s.startsWith("No sales data found")));
    }

    @Test
    void branchFilterNarrowsRows() {
        controller.monthEntered("2026-09");
        controller.branchChosen("rep");
        assertEquals(1, model.visibleTransactions().size());
        assertEquals("REP", model.branch());
    }
}
