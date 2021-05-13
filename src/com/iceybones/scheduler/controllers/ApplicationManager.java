package com.iceybones.scheduler.controllers;

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

/**
 * The application's Main controller. Used to manage the stage and load scenes.
 */
public class ApplicationManager extends Application {

  private static Stage stage;
  private static final ResourceBundle rb = ResourceBundle
      .getBundle("resources/strings", Locale.getDefault());

  /**
   * The main entry point for all JavaFX applications. The start method is called after the init
   * method has returned, and after the system is ready for the application to begin running. NOTE:
   * This method is called on the JavaFX Application Thread. A lambda expression is used to register
   * an event handler in the call to <code>setOnCLoseRequest</code>. This allows the program to
   * commit any pending changes to the database before exiting.
   *
   * @param stage the primary stage for this application, onto which the application scene can be
   *              set. Applications may create other stages, if needed, but they will not be primary
   *              stages.
   */
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

  /**
   * Loads the requested scene and applies it to the primary stage.
   *
   * @param name the name of the fxml file containing the requested scene's layout. Do not include
   *             the .fxml extension.
   */
  static void setScene(String name) {
    name = "/resources/views/" + name + ".fxml";
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

  /**
   * @return the primary stage
   */
  static Stage getStage() {
    return stage;
  }

  /**
   * Passes command line arguments from class Main to the JavaFX application launcher.
   *
   * @param args command line arguments
   */
  static void main(String[] args) {
    launch(args);
  }
}
