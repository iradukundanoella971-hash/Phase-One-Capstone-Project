module igirepay.igire_capstoneproject {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens igirepay.igire_capstoneproject to javafx.fxml;
    exports igirepay.igire_capstoneproject;
    exports igirepay.igire_capstoneproject.lab1.model;
    exports igirepay.igire_capstoneproject.lab1.exception;
    exports igirepay.igire_capstoneproject.lab1.service;
    exports igirepay.igire_capstoneproject.lab1.util;
}