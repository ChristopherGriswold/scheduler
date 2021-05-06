package com.iceybones.scheduler.controllers;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class MainController implements Initializable {

  private static ExecutorService notifyService = Executors.newSingleThreadExecutor();
  private ResourceBundle resourceBundle;
  private static final ExecutorService dbService = Executors.newSingleThreadExecutor();
  private static final Image addImg = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/add_icon.png")));
  private static final Image deleteImg = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/blue_remove_icon.png")));
  private static final Image editImg = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/edit_icon.png")));
  private static final Image errorImg = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/remove_icon.png")));
  private static final Image successImg = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/checkmark_icon.png")));
  private static final Image redClock = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/red_clock.png")));
  private static final Image yellowClockImg = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/yellow_clock.png")));
  private static final Image greenClock = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/green_clock.png")));
  private static final Image blueClock = new Image(Objects.requireNonNull(MainController.class.
      getResourceAsStream("/resources/images/blue_clock.png")));


  public enum NotificationType {
    ADD(addImg), EDIT(editImg), DELETE(deleteImg), SUCCESS(successImg),
    ERROR(errorImg), UPCOMING_APP(yellowClockImg), NONE_UPCOMING(greenClock);
    Image image;

    NotificationType(Image image) {
      this.image = image;
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    resourceBundle = rb;
    appTab.setText(rb.getString("Appointments"));
    custTab.setText(rb.getString("Customers"));
    notificationBar.setText(rb.getString("Notifications"));
    Stage stage = ApplicationManager.getStage();
    stage.setResizable(true);
    stage.setMinWidth(512);
    stage.setMinHeight(384);
    stage.setWidth(1024);
    stage.setHeight(768);
    stage.setTitle("iceybones Scheduler");
    appTabController.setMainController(this);
    custTabController.setMainController(this);
    appTabController.populate();
    custTabController.populate();
  }

  static void stopNotifyService() {
    notifyService.shutdown();
  }

  static void stopDbService() {
    dbService.shutdown();
  }

  static void cancelNotify() {
    notifyService.shutdownNow();
    notifyService = Executors.newSingleThreadExecutor();
  }

  public void notify(String message, NotificationType type, Boolean undoable) {
    cancelNotify();
    notificationBar.setExpanded(true);
    notificationLbl.setText(message);
    notificationImg.setImage(type.image);
    undoLink.setVisible(undoable);
    notifyService.submit(() -> {
      try {
        Thread.sleep(5000);
        Platform.runLater(() -> notificationBar.setExpanded(false));
      } catch (InterruptedException ignored) {

      }
    });
    notifyService.shutdown();
  }

  void refresh() {
    undoLink.setVisible(false);
  }

  static ExecutorService getDbService() {
    return dbService;
  }

  static Image getAddImg() {
    return addImg;
  }

  static Image getDeleteImg() {
    return deleteImg;
  }

  static Image getEditImg() {
    return editImg;
  }

  static Image getRedClock() {
    return redClock;
  }

  static Image getYellowClockImg() {
    return yellowClockImg;
  }

  static Image getGreenClock() {
    return greenClock;
  }

  static Image getBlueClock() {
    return blueClock;
  }

  ProgressIndicator getTableProgress() {
    return tableProgress;
  }

  AppTabController getAppTabController() {
    return appTabController;
  }

  TabPane getTabPane() {
    return tabPane;
  }

  @FXML
  private void onActionUndoLink() {
    tableProgress.setVisible(true);
    appTabController.resetToolButtons();
    appTabController.setCollapseToolDrawer(true);
    custTabController.resetToolButtons();
    custTabController.setCollapseToolDrawer(true);
    dbService.submit(() -> {
      try {
        Database.rollback();
        Platform.runLater(() -> {
          custTabController.populateTable();
          appTabController.populateTable();
          notify(resourceBundle.getString("Undo Successful"), NotificationType.SUCCESS, false);
        });
      } catch (SQLException e) {
        notify(resourceBundle.getString("Failed to undo. Check connection."), NotificationType.ERROR, false);
      } finally {
        tableProgress.setVisible(false);
      }
    });
  }

  @FXML
  @SuppressWarnings("unused")
  private AppTabController appTabController;

  @FXML
  @SuppressWarnings("unused")
  private CustTabController custTabController;

  @FXML
  private TabPane tabPane;

  @FXML
  private TitledPane notificationBar;

  @FXML
  private ImageView notificationImg;

  @FXML
  private Label notificationLbl;

  @FXML
  private Hyperlink undoLink;

  @FXML
  private ProgressIndicator tableProgress;

  @FXML
  private Tab appTab;

  @FXML
  private Tab custTab;

}
