package edu.itc.salesreport.ui.mvp;

/** Formatted table row for the passive view. */
public record RowItem(
        String date,
        String receipt,
        String product,
        String category,
        String quantity,
        String revenue,
        String discount
) {
    public RowItem(String date, String receipt, String product, String category, String quantity, String revenue) {
        this(date, receipt, product, category, quantity, revenue, "0.00");
    }
}
