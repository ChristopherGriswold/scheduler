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

/**
 * Logic controller pertaining to the main scene.
 */
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

  /**
   * Used by the notification bar to identify which icon should be displayed.
   */
  public enum NotificationType {
    ADD(addImg), EDIT(editImg), DELETE(deleteImg), SUCCESS(successImg),
    ERROR(errorImg), UPCOMING_APP(yellowClockImg), NONE_UPCOMING(greenClock);
    Image image;

    /**
     * Sets the image to the corresponding notification type.
     *
     * @param image the image icon that will be stored
     */
    NotificationType(Image image) {
      this.image = image;
    }
  }

  /**
   * Sets up the scene GUI components.
   *
   * @param url the url of the scene's fxml layout
   * @param rb  the currently loaded resource bundle
   */
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
    appTabController.checkForUpcomingApp();
  }

  /**
   * Shuts down the notification service.
   */
  static void stopNotifyService() {
    notifyService.shutdown();
  }

  /**
   * Shuts down the database service.
   */
  static void stopDbService() {
    dbService.shutdown();
  }

  /**
   * Forces the notification service to shutdown now and spawn a new thread to be used in it's
   * place. This stops the notification bar from finishing it's cycle and closing at an inopportune
   * time, namely when a second notification has been issued.
   */
  static void cancelNotify() {
    notifyService.shutdownNow();
    notifyService = Executors.newSingleThreadExecutor();
  }

  /**
   * Open the notification bar, display a message for five seconds and then close.
   *
   * @param message  the message that will be displayed to the user
   * @param type     the notification type that determines which icon will be displayed
   * @param undoable sets whether or not the <code>undo</code> hyperlink will be displayed
   */
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

  /**
   * Disable the <code>undo</code> hyperlink.
   */
  void disableUndo() {
    undoLink.setVisible(false);
  }

  /**
   * @return the service that has the sole responsibility of communicating with the database via
   * static methods in the Database class.
   */
  static ExecutorService getDbService() {
    return dbService;
  }

  /**
   * @return the add icon image
   */
  static Image getAddImg() {
    return addImg;
  }

  /**
   * @return the delete icon image
   */
  static Image getDeleteImg() {
    return deleteImg;
  }

  /**
   * @return the edit icon image
   */
  static Image getEditImg() {
    return editImg;
  }

  /**
   * @return the red clock icon image
   */
  static Image getRedClock() {
    return redClock;
  }

  /**
   * @return the yellow clock icon image
   */
  static Image getYellowClockImg() {
    return yellowClockImg;
  }

  /**
   * @return the green clock icon image
   */
  static Image getGreenClock() {
    return greenClock;
  }

  /**
   * @return the blue clock icon image
   */
  static Image getBlueClock() {
    return blueClock;
  }

  /**
   * @return the table progress indicator
   */
  ProgressIndicator getTableProgress() {
    return tableProgress;
  }

  /**
   * @return the application tab controller
   */
  AppTabController getAppTabController() {
    return appTabController;
  }

  /**
   * @return the main tab pane
   */
  TabPane getTabPane() {
    return tabPane;
  }

  //////////////Event Handlers///////////////////

  /**
   * Rollback the database when the <code>undo</code> hyperlink is clicked. Two lambda expressions
   * are used to implement the <code>Runnable</code> functional interface and help with thread
   * management.
   */
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
          custTabController.populate();
          appTabController.populate();
          notify(resourceBundle.getString("Undo Successful"), NotificationType.SUCCESS, false);
        });
      } catch (SQLException e) {
        notify(resourceBundle.getString("Failed to undo. Check connection."),
            NotificationType.ERROR, false);
      } finally {
        tableProgress.setVisible(false);
      }
    });
  }

  /////////////////GUI Components///////////////////
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
