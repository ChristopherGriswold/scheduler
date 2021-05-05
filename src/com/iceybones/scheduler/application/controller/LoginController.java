package com.iceybones.scheduler.application.controller;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginController implements Initializable {

  private static ResourceBundle resourceBundle;
  private static final Path activityPath = Path.of("login_activity.txt");
  private static final ExecutorService dbService = Executors.newSingleThreadExecutor();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    resourceBundle = rb;
    Stage stage = ApplicationManager.getStage();
    stage.setTitle(rb.getString("Login"));
    stage.setResizable(false);
    loginBtn.setText(rb.getString("Login"));
    usernameTxt.setPromptText(rb.getString("Username"));
    passwordTxt.setPromptText(rb.getString("Password"));
    zoneIdTxt.setText(ZoneId.systemDefault().toString());
    loginBtn.setDisable(true);
    Platform.runLater(() -> borderPane.requestFocus());
  }

  @FXML
  void onActionLogin() {
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
        Platform.runLater(() -> ApplicationManager.setScene("main"));
        dbService.shutdown();
      } catch (Exception e) {
        loginBtn.setDisable(false);
        loginBtn.setVisible(true);
        errorLbl.setVisible(true);
        progressBar.setVisible(false);
        Platform.runLater(() -> errorLbl.setText(resourceBundle.getString(e.getMessage())));
      } finally {
        try (var activityWrite = Files.newBufferedWriter(activityPath, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
          String result =
              "Attempted login : " + LocalDateTime.now() + " : [username='" + usernameTxt.getText()
                  +
                  "', isSuccessful=" + isSuccessful + "]\n";
          activityWrite.write(result);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @FXML
  void onActionPassword() {
    borderPane.requestFocus();
    loginBtn.fire();
  }

  @FXML
  void onActionUsername() {
    borderPane.requestFocus();
    loginBtn.fire();
  }

  @FXML
  void onUsernameKeyTyped() {
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
