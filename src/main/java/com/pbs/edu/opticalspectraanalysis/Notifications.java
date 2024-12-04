package com.pbs.edu.opticalspectraanalysis;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Notifications extends Thread {
    public static void setNotificationDownloadComplete(String downloadFileLocation) {
        Image img = new Image("/confirm.png");
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Download Complete")
                .text("Image saved at " + downloadFileLocation)
                .graphic(new ImageView(img))
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::show);
    }
    public static void setNotificationDownloadFailed() {
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Download Failed")
                .text("Image failed to save!")
                .graphic(null)
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::showError);
    }
    public static void setNotificationTransferToDatabaseComplete(String CSVFileLocation) {
        Image img = new Image("/confirm.png");
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Data Transfer Complete")
                .text("Transfer To Database from file " + CSVFileLocation + " completed!")
                .graphic(new ImageView(img))
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::show);
    }
    public static void setNotificationTransferToDatabaseFailed() {
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Data Transfer Failed")
                .text("Transfer To Database Failed!")
                .graphic(null)
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::showError);
    }
    public static void setNotificationConnectionToDatabaseFailed() {
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Connection Failed")
                .text("Connection to Database Failed!")
                .graphic(null)
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::showError);
    }
    public static void setNotificationDownloadResultsPDFComplete(String downloadFileLocation) {
        Image img = new Image("/confirm.png");
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Download Complete")
                .text("PDF file containing results saved at " + downloadFileLocation)
                .graphic(new ImageView(img))
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::show);
    }
    public static void setNotificationDownloadResultsCSVComplete(String downloadFileLocation) {
        Image img = new Image("/confirm.png");
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Download Complete")
                .text("CSV file containing results saved at " + downloadFileLocation)
                .graphic(new ImageView(img))
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::show);
    }
    public static void setNotificationDeletedRecordSucessfully() {
        Image img = new Image("/confirm.png");
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Deleted Record")
                .text("Deleted selected record succesfully !")
                .graphic(new ImageView(img))
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::show);
    }
    public static void setNotificationCalculateResultsSucessfully(int id_wynik_p) {
        Image img = new Image("/confirm.png");
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Calculated Results")
                .text("Results calculated succesfully !" + " \nid_wynik_p:" + id_wynik_p)
                .graphic(new ImageView(img))
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::show);
    }
    public static void setNotificationNoAccessToWriteFolder(String dir) {
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Wrong Download Directory")
                .text("Write access denied for directory - " + dir + " !")
                .graphic(null)
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::showError);
    }
    public static void setNotificationSuccesfullChangeDownloadFolder(String dir) {
        Image img = new Image("/confirm.png");
        org.controlsfx.control.Notifications notifications = org.controlsfx.control.Notifications.create()
                .title("Download Directory Changed")
                .text("Download directory changed to - " + dir + " !")
                .graphic(new ImageView(img))
                .darkStyle()
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .onAction(actionEvent -> {

                });
        Platform.runLater(notifications::show);
    }
}
