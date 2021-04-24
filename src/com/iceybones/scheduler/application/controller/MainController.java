package com.iceybones.scheduler.application.controller;

import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stage stage = ApplicationManager.getStage();
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setTitle("iceybones Scheduler");
    }
}
