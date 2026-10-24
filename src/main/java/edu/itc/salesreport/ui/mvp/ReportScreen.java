package edu.itc.salesreport.ui.mvp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Passive view: no model access, no formatting, no decisions. */
public interface ReportScreen {
    void showRows(List<RowItem> rows);
    void showCategoryRevenue(Map<String, BigDecimal> revenue);
    void showStatus(String text);
    void showError(String message);
}
