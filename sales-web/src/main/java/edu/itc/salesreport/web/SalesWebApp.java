package edu.itc.salesreport.web;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class SalesWebApp {

    public static void main(String[] args) {
        SpringApplication.run(SalesWebApp.class, args);
    }

    @Bean
    public CsvMonthReader csvMonthReader() {
        return new CsvMonthReader(new CsvTransactionParser());
    }

    @Bean
    public SalesAggregator salesAggregator() {
        return new DefaultSalesAggregator();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
