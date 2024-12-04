package com.pbs.edu.opticalspectraanalysis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class OpticalSpectraApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        /* Load UI */
        FXMLLoader fxmlLoader = new FXMLLoader(OpticalSpectraApplication.class.getResource("UI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 700);
        scene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255);-fx-font-size: 12px;");
        UIController controller = fxmlLoader.getController();
        controller.setStage(stage);
        stage.setResizable(false);
        stage.setTitle("Optical Spectra Analysis");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}