package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

import java.time.YearMonth;
import java.util.List;

public record MonthData(YearMonth month, List<SaleTransaction> rows, List<String> rejected) {
    public MonthData {
        rows = List.copyOf(rows);
        rejected = List.copyOf(rejected);
    }
}