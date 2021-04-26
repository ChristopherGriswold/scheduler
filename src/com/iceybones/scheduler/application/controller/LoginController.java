package com.iceybones.scheduler.application.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class LoginController implements Initializable {
    private static ResourceBundle rb;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stage stage = ApplicationManager.getStage();
        stage.setTitle("Login");
        stage.setResizable(false);
//        rb = ResourceBundle.getBundle("com.iceybones.scheduler.application.resources.strings", new Locale("fr"));
        rb = ResourceBundle.getBundle("com.iceybones.scheduler.application.resources.strings", Locale.getDefault());
        loginBtn.setText(rb.getString("Login"));
        usernameTxt.setPromptText(rb.getString("Username"));
        passwordTxt.setPromptText(rb.getString("Password"));
        zoneIdTxt.setText(ZoneId.systemDefault().toString());
        loginBtn.setDisable(true);
        Platform.runLater(() -> borderPane.requestFocus());
    }

    @FXML
    void onActionLogin(ActionEvent event) {
        borderPane.requestFocus();
        loginBtn.setDisable(true);
        loginBtn.setVisible(false);
        errorLbl.setVisible(false);
        progressBar.setVisible(true);
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                if (Database.authenticate(usernameTxt.getText(), passwordTxt.getText())) {
                    Platform.runLater(() -> ApplicationManager.setScene("main"));
                } else {
                    Platform.runLater(() -> errorLbl.setText(rb.getString("Login Failed")));
                }
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setVisible(true);
                    errorLbl.setVisible(true);
                    progressBar.setVisible(false);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> errorLbl.setText(rb.getString("Database Error")));
            }
        });
        service.shutdown();
    }

    @FXML
    void onActionPassword(ActionEvent event) {
        borderPane.requestFocus();
        loginBtn.fire();
    }

    @FXML
    void onActionUsername(ActionEvent event) {
        borderPane.requestFocus();
        loginBtn.fire();
    }

    @FXML
    void onUsernameKeyTyped(KeyEvent event) {
        loginBtn.setDisable(usernameTxt.getText().equals(""));
    }

    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField usernameTxt;

    @FXML
    private PasswordField passwordTxt;

    @FXML
    private Button loginBtn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label errorLbl;

    @FXML
    private Label zoneIdTxt;

}
