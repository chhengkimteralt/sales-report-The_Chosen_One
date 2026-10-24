package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.mvp.ReportPresenter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.Clock;

public final class ReportMvpApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        var model = new SalesModel(reader, aggregator);

        FxReportScreen screen = new FxReportScreen();
        ReportPresenter presenter = new ReportPresenter(
                model,
                screen,
                Path.of("data"),
                Clock.systemDefaultZone()
        );
        screen.attach(presenter);

        stage.setTitle("Angkor Mart Sales Report (MVP)");
        stage.setScene(new Scene(screen.root(), 900, 600));
        stage.show();

        presenter.monthEntered("2026-09");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
