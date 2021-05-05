package com.iceybones.scheduler.application.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ApplicationManager extends Application {

  private static Stage stage;
  private static final ResourceBundle rb = ResourceBundle.
      getBundle("com.iceybones.scheduler.application.resources.strings", Locale.getDefault());

  @Override
  public void start(Stage stage) {
    stage.setOnCloseRequest((a) -> {
      try {
        if (Database.getConnectedUser() != null) {
          Database.commit();
          MainController.stopNotifyService();
          MainController.stopDbService();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });
    ApplicationManager.stage = stage;
    setScene("login");
    stage.show();
  }

  static void setScene(String name) {
    name = "../view/" + name + ".fxml";
    Parent root = null;
    try {
      root = FXMLLoader
          .load(Objects.requireNonNull(ApplicationManager.class.getResource(name)), rb);
    } catch (IOException e) {
      e.printStackTrace();
    }
    stage.setScene(new Scene(Objects.requireNonNull(root)));
    stage.centerOnScreen();
  }

  static Stage getStage() {
    return stage;
  }

  public static void main(String[] args) {
    launch(args);
  }

}
