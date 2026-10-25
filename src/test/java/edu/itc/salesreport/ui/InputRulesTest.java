package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InputRulesTest {
    private final Clock fixed = Clock.fixed(Instant.parse("2026-09-27T03:00:00Z"), ZoneId.of("Asia/Phnom_Penh"));

    @Test
    void validMonthIsTrimmedAndParsed() {
        Validation<YearMonth> v = InputRules.month(" 2026-09 ", fixed);
        assertTrue(v instanceof Validation.Valid<?>);
        assertEquals(YearMonth.of(2026, 9), ((Validation.Valid<YearMonth>) v).value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "2026/09", "2026-9", "Sept", "2026-10"})
    void rejectsInvalidMonths(String input) {
        Validation<?> v = InputRules.month(input, fixed);
        assertTrue(v instanceof Validation.Invalid<?>);
    }

    @Test
    void futureMonthMessage() {
        Validation<?> v = InputRules.month("2099-01", fixed);
        assertTrue(v instanceof Validation.Invalid<?>);
        assertEquals("2099-01 is in the future", ((Validation.Invalid<?>) v).message());
    }

    @Test
    void branchNormalisation() {
        Validation<String> v = InputRules.branch("pnh ");
        assertTrue(v instanceof Validation.Valid<?>);
        assertEquals("PNH", ((Validation.Valid<String>) v).value());
    }
}
