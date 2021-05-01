package com.iceybones.scheduler.application.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable {
    private static ExecutorService notifyService = Executors.newSingleThreadExecutor();
    private static final ExecutorService dbService = Executors.newSingleThreadExecutor();
    private static final Image addImg = new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("../resources/add_icon.png")));
    private static final Image deleteImg = new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("../resources/blue_remove_icon.png")));
    private static final Image editImg = new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("../resources/edit_icon.png")));
    private static final Image errorImg = new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("../resources/remove_icon.png")));
    private static final Image successImg = new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("../resources/checkmark_icon.png")));

    public enum NotificationType {
        ADD(addImg), EDIT(editImg), DELETE(deleteImg), SUCCESS(successImg), ERROR(errorImg);
        Image image;
        NotificationType(Image image) {
            this.image = image;
        }
    }

    public static void stopNotifyService() {
        notifyService.shutdown();
    }
    public static void stopDbService() {
        dbService.shutdown();
    }

    public static void cancelNotify() {
        notifyService.shutdownNow();
        notifyService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stage stage = ApplicationManager.getStage();
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setTitle("iceybones Scheduler");
        appTabController.setMainController(this);
        custTabController.setMainController(this);
        appTabController.populate();
        custTabController.populate();
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
            } catch (InterruptedException e) {

            }
        });
        notifyService.shutdown();
    }

    @FXML
    void onActionUndoLink(ActionEvent event) {
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
                    notify("Undo Successful", NotificationType.SUCCESS, false);
                });
            } catch (SQLException e) {
                notify("Failed to undo. Check connection.", NotificationType.ERROR, false);
            } finally {
                tableProgress.setVisible(false);
            }
        });
    }

    public void refresh() {
        undoLink.setVisible(false);
    }

    public static ExecutorService getDbService() {
        return dbService;
    }

    public static Image getAddImg() {
        return addImg;
    }

    public static Image getDeleteImg() {
        return deleteImg;
    }

    public static Image getEditImg() {
        return editImg;
    }

    public static Image getErrorImg() {
        return errorImg;
    }

    public static Image getSuccessImg() {
        return successImg;
    }

    public ProgressIndicator getTableProgress() {
        return tableProgress;
    }

    @FXML
    private AppTabController appTabController;

    @FXML
    private CustTabController custTabController;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab appTab;

    @FXML
    private Tab custTab;

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

}
