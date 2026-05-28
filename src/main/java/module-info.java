module igirepay.igire_capstoneproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.postgresql.jdbc;

    opens igirepay.igire_capstoneproject.lab3.controller to javafx.fxml;

    exports igirepay.igire_capstoneproject.lab3;
    exports igirepay.igire_capstoneproject.lab1.model;
    exports igirepay.igire_capstoneproject.lab1.exception;
    exports igirepay.igire_capstoneproject.lab1.service;
    exports igirepay.igire_capstoneproject.lab1.util;
}
