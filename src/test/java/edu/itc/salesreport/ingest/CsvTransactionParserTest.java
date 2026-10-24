package edu.itc.salesreport.ingest;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import edu.itc.salesreport.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CsvTransactionParserTest {
    private final CsvTransactionParser parser = new CsvTransactionParser();

    @Test
    void parsesCanonicalRow() throws InvalidRowException {
        var line = "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR";
        var tx = parser.parse(line);
        assertEquals(new BigDecimal("13.00"), tx.revenue());
        assertEquals(PaymentMethod.KHQR, tx.paymentMethod());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "XYZ,2026-09-01,XX-001,SKU-1,Prod,Unknown,1,1.00,0.00,KHQR", // unknown branch
        "PNH,2026/09/01,PNH-001,SKU-1,Prod,Grocery,1,1.00,0.00,KHQR", // bad date
        "PNH,2026-09-01,PNH-001,SKU-1,Prod,Grocery,0,1.00,0.00,KHQR", // qty 0
        "PNH,2026-09-01,PNH-001,SKU-1,Prod,Grocery,1,1.00,2.00,KHQR", // discount > amount
        "PNH,2026-09-01,PNH-001,SKU-1,Prod,Unknown,1,1.00,0.00,KHQR", // unknown category
        "PNH,2026-09-01,PNH-001,SKU-1,Prod,Grocery,1,1.00,0.00,UNKNOWN", // unknown payment
        "PNH,2026-09-01,PNH-001,SKU-1,Prod,Grocery,1,1.00" // missing fields
    })
    void rejectsInvalidRows(String line) {
        assertThrows(InvalidRowException.class, () -> parser.parse(line));
    }
}
