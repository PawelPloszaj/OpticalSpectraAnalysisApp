module com.pbs.edu.opticalspectraanalysis {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires opencsv;
    requires java.datatransfer;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires java.prefs;

    opens com.pbs.edu.opticalspectraanalysis to javafx.fxml;
    exports com.pbs.edu.opticalspectraanalysis;
}