package edu.itc.salesreport.ui;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesModelTest {
    @TempDir
    Path tempDir;

    private SalesModel model;
    private List<SalesEvent> events;
    private SalesModelListener listener;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(tempDir);
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        model = new SalesModel(reader, aggregator);
        events = new ArrayList<>();
        listener = events::add;
        model.addListener(listener);
    }

    @Test
    void testLoadMonthFiresEventAndComputesChainRevenue() throws IOException {
        model.load(tempDir, YearMonth.of(2026, 9));

        assertEquals(1, events.size());
        assertEquals(new SalesEvent.MonthLoaded(YearMonth.of(2026, 9), 3, 1), events.get(0));

        var summary = model.summary();
        assertEquals("ALL", summary.branch());
        assertEquals(new BigDecimal("52.00"), summary.revenue());
    }

    @Test
    void testSelectBranchTwiceFiresOnceAndComputesBranchRevenueAndBasket() throws IOException {
        model.load(tempDir, YearMonth.of(2026, 9));
        events.clear();

        model.selectBranch("PNH");
        model.selectBranch("PNH");

        assertEquals(1, events.size());
        assertEquals(new SalesEvent.BranchSelected("PNH"), events.get(0));

        var summary = model.summary();
        assertEquals("PNH", summary.branch());
        assertEquals(new BigDecimal("31.00"), summary.revenue());
        assertEquals(new BigDecimal("15.50"), summary.averageBasket());
    }

    @Test
    void testFailedLoadThrowsFiresNothingAndKeepsData() throws IOException {
        model.load(tempDir, YearMonth.of(2026, 9));
        events.clear();

        assertThrows(NoSuchFileException.class, () -> model.load(tempDir, YearMonth.of(2026, 7)));

        assertTrue(events.isEmpty());
        assertEquals(3, model.visibleTransactions().size());
    }

    @Test
    void testRemovedListenerIsNotCalled() throws IOException {
        model.removeListener(listener);
        model.load(tempDir, YearMonth.of(2026, 9));

        assertTrue(events.isEmpty());
    }
}