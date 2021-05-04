package com.iceybones.scheduler.application.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class ApplicationManager extends Application {

  private static Stage stage;

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
    //TODO REMOVE BEFORE ALL SAID AND DONE
//    try {
//      Database.login("test", "test");
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
    setScene("login");
    stage.show();
  }

  public static Stage getStage() {
    return stage;
  }

  public static void setScene(String name) {
    name = "../view/" + name + ".fxml";
    Parent root = null;
    try {
      root = FXMLLoader.load(Objects.requireNonNull(ApplicationManager.class.getResource(name)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    stage.setScene(new Scene(root));
    stage.centerOnScreen();
  }


  public static void main(String[] args) {
    launch(args);
  }

}
