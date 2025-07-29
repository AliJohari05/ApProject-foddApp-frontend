module com.foodapp.food4ufrontend {
    // Requires statements for JavaFX modules
    requires javafx.web;
    requires javafx.swing;
    requires javafx.controls;
    requires javafx.fxml;

    // Requires statements for Jackson (JSON processing)
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.annotation;

    // Requires statements for other UI libraries
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.jfoenix;
    requires java.net.http;
    requires MaterialFX;

    // Export the main package so LauncherImpl can access Food4uApp
    exports com.foodapp.food4ufrontend;

    // Exports for controllers (needed for instantiation)
    exports com.foodapp.food4ufrontend.controller.dashbord to javafx.fxml;

    exports com.foodapp.food4ufrontend.dto to com.fasterxml.jackson.databind;
    // Opens statements for FXML controllers and models for Jackson
    // This allows JavaFX's FXMLLoader to access FXML files within these packages
    opens com.foodapp.food4ufrontend.controller.login to javafx.fxml;
    opens com.foodapp.food4ufrontend.controller.signup to javafx.fxml;
    opens com.foodapp.food4ufrontend.controller.dashbord to javafx.fxml, javafx.base, javafx.controls;

    opens com.foodapp.food4ufrontend.model to com.fasterxml.jackson.databind, javafx.base;
    opens com.foodapp.food4ufrontend.util to com.fasterxml.jackson.databind;
    opens com.foodapp.food4ufrontend.css to javafx.fxml;
    opens com.foodapp.food4ufrontend.images to javafx.fxml;

    // FIX: ADD THIS LINE to open the 'view.dashbord' package for FXML includes
    opens com.foodapp.food4ufrontend.view.dashbord to javafx.fxml;
}