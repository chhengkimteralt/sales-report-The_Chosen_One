package edu.itc.salesreport;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Scans the core packages (model, ingest, engine, ui, ui/mvp) directly
 * and fails if any JavaFX, AWT or Swing import is found.
 * Note: ui/fx and ui/console are UI toolkit packages and not part of core.
 */
class UiIndependenceTest {
    private static final List<String> FORBIDDEN = List.of(
            "import javafx.",
            "import java.awt.",
            "import javax.swing.");

    private static final List<String> CORE_DIRS = List.of(
            "model",
            "ingest",
            "engine",
            "ui",
            "ui/mvp"
    );

    @Test
    void coreHasNoToolkitImports() throws IOException {
        Path base = Path.of("src/main/java/edu/itc/salesreport");
        List<Path> javaFiles = new ArrayList<>();

        for (String dirName : CORE_DIRS) {
            Path dir = base.resolve(dirName);
            if (Files.exists(dir) && Files.isDirectory(dir)) {
                try (Stream<Path> stream = Files.list(dir)) {
                    stream.filter(p -> p.toString().endsWith(".java"))
                          .forEach(javaFiles::add);
                }
            }
        }

        for (Path f : javaFiles) {
            var lines = Files.readAllLines(f);
            for (String line : lines) {
                for (String bad : FORBIDDEN) {
                    if (line.trim().startsWith(bad)) {
                        fail("Toolkit import found in core file: " + f + " -> " + line);
                    }
                }
            }
        }
    }
}
