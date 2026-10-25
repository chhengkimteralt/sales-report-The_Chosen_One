package edu.itc.salesreport.web;

import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ui.InputRules;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.Validation;
import edu.itc.salesreport.ui.Validation.Invalid;
import edu.itc.salesreport.ui.Validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

@Controller
public class ReportWebController {
    private record Inputs(Validation<YearMonth> month, Validation<String> branch) {}

    private final CsvMonthReader reader;
    private final SalesAggregator aggregator;
    private final Clock clock;
    private final Path dataDir;

    public ReportWebController(CsvMonthReader reader,
                              SalesAggregator aggregator,
                              Clock clock,
                              @Value("${salesreport.data-dir:data}") String dataDir) {
        this.reader = reader;
        this.aggregator = aggregator;
        this.clock = clock;
        // a String, not a Path: Spring would resolve a relative Path
        // against the servlet context instead of the working directory
        this.dataDir = Path.of(dataDir);
    }

    @GetMapping("/report")
    public String report(@RequestParam(defaultValue = "2026-09") String month,
                        @RequestParam(defaultValue = "ALL") String branch,
                        Model page) {
        page.addAttribute("month", month);
        page.addAttribute("branch", branch);
        page.addAttribute("branches", List.of("ALL", "PNH", "REP", "BTB"));
        var inputs = new Inputs(InputRules.month(month, clock), InputRules.branch(branch));
        switch (inputs) {
            case Inputs(Valid<YearMonth>(var m), Valid<String>(var b)) -> show(m, b, page);
            case Inputs(Invalid<YearMonth>(var msg), _) -> page.addAttribute("error", msg);
            case Inputs(_, Invalid<String>(var msg)) -> page.addAttribute("error", msg);
        }
        return "report";
    }

    private void show(YearMonth month, String branch, Model page) {
        try {
            var model = new SalesModel(reader, aggregator);
            model.load(dataDir, month);
            model.selectBranch(branch);
            page.addAttribute("summary", model.summary());
            page.addAttribute("rejected", model.rejectedRows());
        } catch (NoSuchFileException e) {
            page.addAttribute("error", "No sales data found in " + e.getMessage());
        } catch (IOException e) {
            page.addAttribute("error", "Could not read the sales files: " + e.getMessage());
        }
    }
}
