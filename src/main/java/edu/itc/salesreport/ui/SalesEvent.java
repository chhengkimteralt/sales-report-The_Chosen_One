package edu.itc.salesreport.ui;

import edu.itc.salesreport.model.SaleTransaction;

import java.time.YearMonth;
import java.util.List;

public sealed interface SalesEvent permits SalesEvent.MonthLoaded, SalesEvent.BranchSelected {
    record MonthLoaded(YearMonth month, int rowCount, int rejectedCount) implements SalesEvent {
    }

    record BranchSelected(String branch) implements SalesEvent {
    }
}