package com.iceybones.scheduler.application.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginController implements Initializable {
    private static ResourceBundle rb;
    private static final Path activityPath = Path.of("login_activity.txt");
    private static final ExecutorService dbService = Executors.newSingleThreadExecutor();
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
        dbService.submit(() -> {
            boolean isSuccessful = false;
            try {
                Database.login(usernameTxt.getText(), passwordTxt.getText());
                isSuccessful = true;
            } catch (Exception e) {
                Platform.runLater(() -> errorLbl.setText(rb.getString(e.getMessage())));
            } finally {
                try (var activityWrite = Files.newBufferedWriter(activityPath, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    String result = "Attempted login : " + LocalDateTime.now() + " : [username='" + usernameTxt.getText() +
                            "', isSuccessful=" + isSuccessful + "]\n";
                    activityWrite.write(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> {
                ApplicationManager.setScene("main");
                loginBtn.setDisable(false);
                loginBtn.setVisible(true);
                errorLbl.setVisible(true);
                progressBar.setVisible(false);
            });
        });
        dbService.shutdown();
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
