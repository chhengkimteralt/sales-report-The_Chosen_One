package edu.itc.salesreport.ui.mvp;

import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.InputRules;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import edu.itc.salesreport.ui.Validation;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

/** MVP presenter: handles input and pushes formatted data. */
public final class ReportPresenter implements SalesModelListener {
    private final SalesModel model;
    private final ReportScreen screen;
    private final Path dataDir;
    private final Clock clock;

    public ReportPresenter(SalesModel model, ReportScreen screen,
                           Path dataDir, Clock clock) {
        this.model = model;
        this.screen = screen;
        this.dataDir = dataDir;
        this.clock = clock;
        model.addListener(this);
    }

    public void monthEntered(String text) {
        switch (InputRules.month(text, clock)) {
            case Validation.Valid<YearMonth>(var month) -> {
                try {
                    model.load(dataDir, month);
                    screen.showError("");
                } catch (IOException e) {
                    screen.showError("No sales data for " + month);
                }
            }
            case Validation.Invalid<YearMonth>(var message) -> screen.showError(message);
        }
    }

    public void branchChosen(String text) {
        switch (InputRules.branch(text)) {
            case Validation.Valid<String>(var branch) -> {
                model.selectBranch(branch);
                screen.showError("");
            }
            case Validation.Invalid<String>(var message) -> screen.showError(message);
        }
    }

    @Override
    public void modelChanged(SalesEvent event) {
        var s = model.summary();
        List<RowItem> rows = model.visibleTransactions().stream()
                .map(this::toRow)
                .toList();
        screen.showRows(rows);
        screen.showCategoryRevenue(s.revenueByCategory());

        String statusText = "%s · %d receipts · revenue USD %s · basket USD %s".formatted(
                s.branch(),
                s.receipts(),
                money(s.revenue()),
                money(s.averageBasket())
        );
        screen.showStatus(statusText);
        screen.showError("");
    }

    private RowItem toRow(SaleTransaction tx) {
        return new RowItem(
                tx.date().toString(),
                tx.receiptNo(),
                tx.productName(),
                tx.category(),
                String.valueOf(tx.quantity()),
                money(tx.revenue()),
                money(tx.discount())
        );
    }

    public static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
