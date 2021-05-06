package com.iceybones.scheduler.controllers;

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

/**
 * Logic controller pertaining to the login scene.
 */
public class LoginController implements Initializable {

  private static ResourceBundle resourceBundle;
  private static final Path activityPath = Path.of("login_activity.txt");
  private static final ExecutorService dbService = Executors.newSingleThreadExecutor();

  /**
   * Sets up the scene GUI components. A lambda expression is used to implement the runnable
   * interface for the <code>Platform.runLater</code> method to have the JavaFX thread alter the
   * focus so that the <code>userName</code> text field is not selected, allowing it to display it's
   * prompt text.
   *
   * @param url the url of the scene's fxml layout
   * @param rb  the currently loaded resource bundle
   */
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

  ////////////////Event Handlers//////////////////////

  /**
   * Attempts to login to the application as a result of the <code>login</code> button being
   * pressed. The details and result of the login attempt is appended to a log file.
   */
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

  /**
   * Fires the login button when the user presses <code>ENTER</code> while the cursor is in the
   * password field.
   */
  @FXML
  void onActionPassword() {
    borderPane.requestFocus();
    loginBtn.fire();
  }

  /**
   * Fires the login button when the user presses <code>ENTER</code> while the cursor is in the
   * username field.
   */
  @FXML
  void onActionUsername() {
    borderPane.requestFocus();
    loginBtn.fire();
  }

  /**
   * Activates the login button when there is text in the username field and otherwise disables it.
   */
  @FXML
  void onUsernameKeyTyped() {
    loginBtn.setDisable(usernameTxt.getText().equals(""));
  }

  //////////////////////GUI Components//////////////////
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
