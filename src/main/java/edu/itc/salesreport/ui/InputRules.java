package edu.itc.salesreport.ui;

import edu.itc.salesreport.ui.SalesModel;

import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Set;

public final class InputRules {
    private static final Set<String> BRANCHES =
            Set.of(SalesModel.ALL, "PNH", "REP", "BTB");

    private InputRules() {}

    public static Validation<YearMonth> month(String text, Clock clock) {
        if (text == null || text.isBlank()) {
            return new Validation.Invalid<>("Enter a month as yyyy-MM, e.g. 2026-09");
        }
        YearMonth parsed;
        try {
            parsed = YearMonth.parse(text.strip());
        } catch (DateTimeParseException e) {
            return new Validation.Invalid<>("'" + text.strip() + "' is not a month in the form yyyy-MM");
        }
        if (parsed.isAfter(YearMonth.now(clock))) {
            return new Validation.Invalid<>(parsed + " is in the future");
        }
        return new Validation.Valid<>(parsed);
    }

    public static Validation<String> branch(String text) {
        if (text == null) {
            return new Validation.Invalid<>("Unknown branch '': use ALL, PNH, REP or BTB");
        }
        String code = text.strip().toUpperCase();
        if (!BRANCHES.contains(code)) {
            return new Validation.Invalid<>("Unknown branch '" + text.strip() + "': use ALL, PNH, REP or BTB");
        }
        return new Validation.Valid<>(code);
    }
}