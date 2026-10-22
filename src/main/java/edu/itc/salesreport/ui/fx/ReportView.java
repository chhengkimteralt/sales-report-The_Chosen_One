package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;

import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;

public final class ReportView implements SalesModelListener, ErrorDisplay {
    private final SalesModel model;
    private final BorderPane root = new BorderPane();
    private final TextField monthField = new TextField("2026-09");
    private final ComboBox<String> branchBox = new ComboBox<>();
    private final TableView<SaleTransaction> table = new TableView<>();
    private final BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final Label status = new Label("No month loaded");
    private final Label error = new Label();

    public ReportView(SalesModel model) {
        this.model = model;
        branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");

        table.getColumns().setAll(List.of(
                column("Date", t -> t.date().toString()),
                column("Receipt", SaleTransaction::receiptNo),
                column("Product", SaleTransaction::productName),
                column("Category", SaleTransaction::category),
                column("Qty", t -> String.valueOf(t.quantity())),
                column("Revenue", t -> t.revenue().toPlainString())));

        chart.setTitle("Revenue by category (USD)");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        error.setStyle("-fx-text-fill: firebrick;");

        HBox filters = new HBox(10, new Label("Month:"), monthField, new Label("Branch:"), branchBox);
        filters.setPadding(new Insets(10));

        SplitPane splitPane = new SplitPane(table, chart);
        splitPane.setDividerPositions(0.6);

        VBox bottom = new VBox(5, status, error);
        bottom.setPadding(new Insets(10));

        root.setTop(filters);
        root.setCenter(splitPane);
        root.setBottom(bottom);

        model.addListener(this);
    }

    public Parent root() {
        return root;
    }

    @Override
    public void modelChanged(SalesEvent event) {
        branchBox.setValue(model.branch());
        table.getItems().setAll(model.visibleTransactions());

        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        model.summary().revenueByCategory().forEach((cat, rev) -> series.getData().add(new XYChart.Data<>(cat, rev)));
        chart.getData().add(series);

        String text = switch (event) {
            case SalesEvent.MonthLoaded(var month, var rows, var rejected) ->
                "Loaded %s: %d rows, %d rejected".formatted(month, rows, rejected);
            case SalesEvent.BranchSelected(var branch) ->
                "Branch filter: " + branch;
        };
        status.setText(text);
    }

    @Override
    public void showError(String message) {
        error.setText(message);
    }

    @Override
    public void clearError() {
        error.setText("");
    }

    private static TableColumn<SaleTransaction, String> column(String title,
            Function<SaleTransaction, String> value) {
        var col = new TableColumn<SaleTransaction, String>(title);
        col.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(value.apply(c.getValue())));
        return col;
    }
}