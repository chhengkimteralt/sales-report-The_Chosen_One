package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.SimpleReportEngine;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewerScreenshotTest {

    private static boolean jfxStarted = false;

    private synchronized void startJfx() {
        if (!jfxStarted) {
            try {
                Platform.startup(() -> {});
                jfxStarted = true;
            } catch (IllegalStateException e) {
                jfxStarted = true;
            }
        }
    }

    @Test
    void captureViewerScreenshot() throws Exception {
        startJfx();

        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try {
                Path dataDir = Path.of("data");
                var engine = new SimpleReportEngine();
                SummarySource source = (month, branch) -> engine.summarizeBranch(month, branch, dataDir);
                var vm = new ReportViewModel(source, new CallerThreadJobs(), Clock.systemDefaultZone());

                ReportView view = new ReportView(vm);
                Scene scene = new Scene(view.root(), 960, 600);

                // Load 2026-08 for all branches
                vm.monthProperty().set(YearMonth.of(2026, 8));
                vm.generateCommand().execute();

                // Trigger CSS and layout passes
                view.root().applyCss();
                view.root().layout();

                WritableImage image = scene.snapshot(null);
                int width = (int) image.getWidth();
                int height = (int) image.getHeight();
                BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
                    }
                }
                Path outDir = Path.of("lab03", "screenshots");
                Files.createDirectories(outDir);
                Path outFile = outDir.resolve("viewer.png");
                ImageIO.write(bImage, "png", outFile.toFile());
                System.out.println("Saved screenshot to: " + outFile.toAbsolutePath());
            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
        if (error[0] != null) {
            throw new RuntimeException(error[0]);
        }
        assertTrue(Files.exists(Path.of("lab03", "screenshots", "viewer.png")));
    }
}
