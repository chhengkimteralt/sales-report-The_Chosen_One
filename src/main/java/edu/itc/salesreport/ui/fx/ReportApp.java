package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;

public final class ReportApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        var model = new SalesModel(reader, aggregator);

        ReportView view = new ReportView(model);

        var controller = new ReportController(
                model, 
                Path.of("data"), 
                Clock.systemDefaultZone(), 
                view
        );
        view.bind(controller);

        stage.setTitle("Angkor Mart Sales Report");
        stage.setScene(new Scene(view.root(), 900, 600));
        stage.show();

        controller.monthEntered("2026-09");
    }

    public static void main(String[] args) {
        launch(args);
    }
}