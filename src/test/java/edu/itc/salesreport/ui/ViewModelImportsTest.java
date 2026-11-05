package edu.itc.salesreport.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ViewModelImportsTest {

    @Test
    void viewModelImportsOnlyBeansAndCollections() throws IOException {
        Path vmFile = Path.of("src/main/java/edu/itc/salesreport/ui/ReportViewModel.java");
        List<String> lines = Files.readAllLines(vmFile);

        boolean foundJavaFxImport = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("import javafx.")) {
                foundJavaFxImport = true;
                assertTrue(
                        trimmed.startsWith("import javafx.beans.") || trimmed.startsWith("import javafx.collections."),
                        "Disallowed import in ReportViewModel: " + trimmed
                );
            }
            if (trimmed.startsWith("import java.awt.") || trimmed.startsWith("import javax.swing.")) {
                fail("Disallowed toolkit import: " + trimmed);
            }
        }
        assertTrue(foundJavaFxImport, "Expected at least one javafx import in ReportViewModel");
    }
}
