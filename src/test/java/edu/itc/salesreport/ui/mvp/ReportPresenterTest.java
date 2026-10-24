package edu.itc.salesreport.ui.mvp;

import static org.junit.jupiter.api.Assertions.*;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesModel;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportPresenterTest {
    static final class FakeScreen implements ReportScreen {
        String status = "";
        String error = "";
        List<RowItem> rows = new ArrayList<>();
        Map<String, BigDecimal> categoryRevenue = Map.of();

        @Override
        public void showRows(List<RowItem> rows) {
            this.rows = rows;
        }

        @Override
        public void showCategoryRevenue(Map<String, BigDecimal> revenue) {
            this.categoryRevenue = revenue;
        }

        @Override
        public void showStatus(String text) {
            this.status = text;
        }

        @Override
        public void showError(String message) {
            this.error = message;
        }
    }

    @TempDir
    Path dataDir;

    private final FakeScreen screen = new FakeScreen();
    private SalesModel model;
    private ReportPresenter presenter;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(dataDir);
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        model = new SalesModel(reader, aggregator);
        Clock clock = Clock.fixed(Instant.parse("2026-09-27T03:00:00Z"), ZoneId.of("Asia/Phnom_Penh"));
        presenter = new ReportPresenter(model, screen, dataDir, clock);
    }

    @Test
    void testPresenterFormatsStatusAndRowsCorrectly() {
        presenter.monthEntered("2026-09");
        presenter.branchChosen("PNH");

        assertEquals("PNH · 1 receipts · revenue USD 31.00 · basket USD 31.00", screen.status);

        assertTrue(screen.rows.size() >= 2);
        RowItem secondRow = screen.rows.get(1);
        assertEquals("2026-09-01", secondRow.date());
        assertEquals("PNH-000001", secondRow.receipt());
        assertEquals("Power Bank 10000mAh", secondRow.product());
        assertEquals("Electronics", secondRow.category());
        assertEquals("1", secondRow.quantity());
        assertEquals("18.00", secondRow.revenue());
    }

    @Test
    void testMoneyFormatting() {
        assertEquals("1,234.50", ReportPresenter.money(new BigDecimal("1234.5")));
        assertEquals("0.00", ReportPresenter.money(BigDecimal.ZERO));
    }
}
