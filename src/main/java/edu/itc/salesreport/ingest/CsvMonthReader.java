package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class CsvMonthReader {
    private static final Pattern FILE_PATTERN = Pattern.compile("^(PNH|REP|BTB)-\\d{4}-\\d{2}-\\d{2}\\.csv$");
    private final TransactionParser parser;

    public CsvMonthReader(TransactionParser parser) {
        this.parser = parser;
    }

    public MonthData read(Path dataDir, YearMonth month) throws IOException {
        Path monthDir = dataDir.resolve(month.toString());
        if (!Files.exists(monthDir) || !Files.isDirectory(monthDir)) {
            throw new NoSuchFileException(monthDir.toString());
        }

        List<SaleTransaction> validRows = new ArrayList<>();
        List<String> rejectedRows = new ArrayList<>();

        List<Path> files;
        try (Stream<Path> stream = Files.list(monthDir)) {
            files = stream
                    .filter(p -> FILE_PATTERN.matcher(p.getFileName().toString()).matches())
                    .sorted((p1, p2) -> p1.getFileName().toString().compareTo(p2.getFileName().toString()))
                    .toList();
        }

        for (Path file : files) {
            String filename = file.getFileName().toString();
            List<String> lines = Files.readAllLines(file);
            if (lines.isEmpty())
                continue;

            // Skip header line
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty())
                    continue;
                try {
                    SaleTransaction tx = parser.parse(line);
                    validRows.add(tx);
                } catch (InvalidRowException e) {
                    rejectedRows.add("%s:%d %s".formatted(filename, i + 1, e.getMessage()));
                }
            }
        }

        return new MonthData(month, validRows, rejectedRows);
    }
}