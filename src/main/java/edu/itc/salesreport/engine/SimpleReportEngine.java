package edu.itc.salesreport.engine;

import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Summarizes sales data by branch for a given month.
 */
public class SimpleReportEngine {
    private final CsvMonthReader reader;
    private final SalesAggregator aggregator;
    private final Map<YearMonth, List<SaleTransaction>> cache = new ConcurrentHashMap<>();

    public SimpleReportEngine() {
        this(new CsvMonthReader(new CsvTransactionParser()), new DefaultSalesAggregator());
    }

    public SimpleReportEngine(CsvMonthReader reader, SalesAggregator aggregator) {
        this.reader = reader;
        this.aggregator = aggregator;
    }

    public BranchSummary summarizeBranch(YearMonth month, String branch, Path dataDir) throws IOException {
        List<SaleTransaction> rows;
        try {
            rows = cache.computeIfAbsent(month, m -> {
                try {
                    return reader.read(dataDir, m).rows();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            throw e;
        }

        List<SaleTransaction> filtered = "ALL".equalsIgnoreCase(branch)
                ? rows
                : rows.stream().filter(t -> branch.equalsIgnoreCase(t.branch())).toList();
        return aggregator.summarize(branch, filtered);
    }
}
