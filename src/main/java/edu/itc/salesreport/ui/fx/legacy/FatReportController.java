package edu.itc.salesreport.ui.fx.legacy;

import edu.itc.salesreport.ui.ReportController;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Smell on purpose: I/O, parsing, filtering and totals in a handler. Refactored to delegate. */
public class FatReportController {
    private final TextField monthField;
    private final ComboBox<String> branchBox;
    private final Label status;
    private ReportController controller;

    public FatReportController(TextField monthField, ComboBox<String> branchBox, Label status) {
        this.monthField = monthField;
        this.branchBox = branchBox;
        this.status = status;
    }

    public void setController(ReportController controller) {
        this.controller = controller;
    }

    public void onLoad() {
        if (controller != null) {
            controller.monthEntered(monthField.getText());
            controller.branchChosen(branchBox.getValue());
        }
    }
}
